package smallville7123.simple.vst.demo.pack.sequencer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;

import static android.media.AudioTrack.WRITE_BLOCKING;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class AudioDeck {
    private static final String TAG = "AudioDeck";
    private final Context mContext;
    private final File filesDir;
    private final ArrayList<AudioTrack> audioFiles = new ArrayList<>();

    public AudioTrack getTrack(int sound_ID) {
        return audioFiles.get(sound_ID);
    }

    private int _load(Path tmp) {
        int sampleRate = 48000;
        int channelCount = 2;
        String converted = tmp + ".converted.f_s16le.ar_" + sampleRate + ".ac_" + channelCount;
        int returnCode = FFmpeg.execute("-y" + " " +
                // input
                "-i " + tmp + " " +
                // output
                "-f s16le" + " " +// audio format is signed 16 bit little-endian pcm
                "-ar " + sampleRate + " " +
                "-ac " + channelCount + " " +
                converted
        );
        if (returnCode == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
            AudioTrack audioTrack = new AudioTrack(new File(converted), sampleRate, channelCount);
            audioFiles.add(audioTrack);
            return audioFiles.indexOf(audioTrack);
        } else {
            throw new RuntimeException(Config.TAG + ": Command execution failed (returned " + returnCode + ")");
        }
    }

    class AudioTrack {
        android.media.AudioTrack track;
        android.media.AudioFormat format;
        android.media.AudioAttributes attributes;
        FileInputStream inputStream;
        File resource;
        long fileSize = 0;
        int mChannelCount;
        int mSampleRate;
        long totalFrames;
        byte[] buffer;
        int bufferSize;
        boolean playing = false;

        public void play() {
            playing = true;
        }

        public void pause() {
            playing = false;
        }

        final Object lock = new Object();

        void reset() {
            synchronized (lock) {
                if (inputStream != null) {
                    requestInputStream();
                } else {
                    head = 0;
                }
            }
        }

        void requestInputStream() {
            try {
                synchronized (lock) {
                    buffer = new byte[bufferSize];
                    data = null;
                    head = 0;
                    inputStream = new FileInputStream(resource);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        // unused
        ByteBuffer data = null;

        void loadIntoMemory() {
            int fs = Math.toIntExact(fileSize);
            synchronized (lock) {
                buffer = new byte[bufferSize];
                requestInputStream();
                byte[] data = new byte[Math.toIntExact(fs)];
                while (true) {
                    int n = 0;
                    try {
                        n = inputStream.read(data, 0, Math.min(bufferSize, (int) (fileSize - n)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (n >= 0) break;
                }
                this.data = ByteBuffer.wrap(data);
                inputStream = null;
                reset();
            }
        }

        int head = 0;

        int readBuffer() {
            synchronized (lock) {
                if (inputStream != null) {
                    int sizeInBytes = Math.min(bufferSize, (int) (fileSize - head));
                    int ret = 0;
                    try {
                        ret = inputStream.read(buffer, 0, sizeInBytes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (ret == -1 || ret == 0) return 0;
                    // native audio track
                    return track.write(buffer, 0, ret, WRITE_BLOCKING);
                } else {
                    // data is ByteBuffer.wrap(data_)
                    int sizeInBytes = Math.min(bufferSize, data.remaining());
                    if (sizeInBytes == 0) return -1;
                    // buffer is byte[] buffer
                    data.get(buffer, 0, sizeInBytes);
                    // native audio track
                    return track.write(buffer, 0, sizeInBytes, WRITE_BLOCKING);
                }
            }
        }

        Thread playbackThread;
        boolean looping = false;

        public AudioTrack(File file, int sampleRate, int channelCount) {
            resource = file;
            mSampleRate = sampleRate;
            mChannelCount = channelCount;
            try {
                fileSize = Files.size(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            totalFrames = fileSize / (mSampleRate * mChannelCount);

            int channelType = AudioFormat.CHANNEL_OUT_STEREO;
            int encoding = AudioFormat.ENCODING_PCM_16BIT;

            format = new AudioFormat.Builder()
                    .setSampleRate(mSampleRate)
                    .setEncoding(encoding)
                    .build();

            attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            track = new android.media.AudioTrack.Builder()
                    .setAudioFormat(format)
                    .setAudioAttributes(attributes)
                    .setTransferMode(android.media.AudioTrack.MODE_STREAM)
                    .setPerformanceMode(android.media.AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .setBufferSizeInBytes(
                            android.media.AudioTrack.getMinBufferSize(
                                    mSampleRate,
                                    channelType,
                                    encoding
                            )
                    )
                    .build();
            bufferSize = track.getBufferSizeInFrames();
            buffer = new byte[bufferSize];
            requestInputStream();
            playbackThread = new Thread(() -> {
                while(true) {
                    if (playing) {
                        if (readBuffer() == -1 && looping) {
                            reset();
                            readBuffer();
                        }
                        track.play();
                    }
                }
            });
            playbackThread.start();
        }
    }

    AudioDeck(Context context) {
        mContext = context;
        filesDir = context.getFilesDir();
    }

    /**
     * Load the sound from the specified path.
     *
     * @param path the path to the audio file
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    public int loadPath(String path) {
        CharSequence extension = path.substring(path.lastIndexOf("."));
        File out = createTemporaryFile(extension);
        Path outPath = out.toPath();
        try {
            Files.copy(Paths.get(path), outPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.io.IOException e) {
            throw new RuntimeException("error loading " + path + ": " + e);
        }
        return _load(outPath);
    }

    /**
     * Load the sound from the specified APK resource.
     *
     * Note that the extension is dropped. For example, if you want to load
     * a sound from the raw resource file "explosion.mp3", you would specify
     * "R.raw.explosion" as the resource ID. Note that this means you cannot
     * have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     * directory.
     *
     * @param resId the resource ID
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    public int load(int resId, CharSequence extension) {
        return load(mContext.getResources().openRawResourceFd(resId), extension);
    }

    /**
     * Load the sound from an asset file descriptor.
     *
     * @param afd an asset file descriptor
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    public int load(AssetFileDescriptor afd, CharSequence extension) {
        Objects.requireNonNull(afd);
        File out = createTemporaryFile(extension);
        Path outPath = out.toPath();
        Utils.copy(afd, outPath);
        return _load(outPath);
    }

    private File createTemporaryFile(CharSequence extension) {
        try {
            return File.createTempFile("TEMPORARY_FILE", extension.toString(), filesDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the sound from a FileDescriptor.
     *
     * @param fd a FileDescriptor object
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    public int load(FileDescriptor fd, CharSequence extension) {
        File out = createTemporaryFile(extension);
        Path outPath = out.toPath();
        Utils.copy(fd, outPath);
        return _load(outPath);
    }

    /**
     * Load the sound from a FileDescriptor.
     *
     * This version is useful if you store multiple sounds in a single
     * binary. The offset specifies the offset from the start of the file
     * and the length specifies the length of the sound within the file.
     *
     * @param fd a FileDescriptor object
     * @param offset offset to the start of the sound
     * @param length length of the sound
     * @param extension the extension that should be used to decode the file.
     *                  Note that this means you cannot
     *                  have both an "explosion.wav" and an "explosion.mp3" in the res/raw
     *                  directory.
     * @return a sound ID. This value can be used to play or unload the sound.
     */
    public int load(FileDescriptor fd, long offset, long length, CharSequence extension) {
        File out = createTemporaryFile(extension);
        Path outPath = out.toPath();
        try {
            FileInputStream fis = new FileInputStream(fd);
            Utils.copy(fis, outPath, offset, length, StandardCopyOption.REPLACE_EXISTING);
            fis.close();
        } catch (java.io.IOException ex) {
            throw new RuntimeException("close failed: " + ex);
        }
        return _load(outPath);
    }

    private static class Utils {

        // buffer size used for reading and writing
        private static final int BUFFER_SIZE = 8192;

        private static void copy(FileDescriptor fd, Path outPath) {
            copy(new FileInputStream(fd), outPath);
        }

        private static void copy(FileInputStream fis, Path outPath) {
            try {
                Files.copy(fis, outPath, StandardCopyOption.REPLACE_EXISTING);
                fis.close();
            } catch (java.io.IOException ex) {
                throw new RuntimeException("close failed: " + ex);
            }
        }

        private static void copy(AssetFileDescriptor fd, Path outPath) {
            Objects.requireNonNull(fd);
            try {
                copy(fd.createInputStream(), outPath);
                fd.close();
            } catch (java.io.IOException ex) {
                throw new RuntimeException("close failed: " + ex);
            }
        }

        /**
         * Reads all bytes from an input stream and writes them to an output stream.
         */
        private static long copy(InputStream source, long offset, long length, OutputStream sink)
                throws IOException
        {
            source.skip(offset);
            long nread = 0L;
            byte[] buf = new byte[BUFFER_SIZE];
            int n = 0;
            while ((n = source.read(buf, 0, Math.min(BUFFER_SIZE, (int) (length - n)))) > 0) {
                sink.write(buf, 0, n);
                nread += n;
            }
            return nread;
        }

        /**
         * Copies all bytes from an input stream to a file. On return, the input
         * stream will be at end of stream.
         *
         * <p> By default, the copy fails if the target file already exists or is a
         * symbolic link. If the {@link StandardCopyOption#REPLACE_EXISTING
         * REPLACE_EXISTING} option is specified, and the target file already exists,
         * then it is replaced if it is not a non-empty directory. If the target
         * file exists and is a symbolic link, then the symbolic link is replaced.
         * In this release, the {@code REPLACE_EXISTING} option is the only option
         * required to be supported by this method. Additional options may be
         * supported in future releases.
         *
         * <p>  If an I/O error occurs reading from the input stream or writing to
         * the file, then it may do so after the target file has been created and
         * after some bytes have been read or written. Consequently the input
         * stream may not be at end of stream and may be in an inconsistent state.
         * It is strongly recommended that the input stream be promptly closed if an
         * I/O error occurs.
         *
         * <p> This method may block indefinitely reading from the input stream (or
         * writing to the file). The behavior for the case that the input stream is
         * <i>asynchronously closed</i> or the thread interrupted during the copy is
         * highly input stream and file system provider specific and therefore not
         * specified.
         *
         * <p> <b>Usage example</b>: Suppose we want to capture a web page and save
         * it to a file:
         * <pre>
         *     Path path = ...
         *     URI u = URI.create("http://java.sun.com/");
         *     try (InputStream in = u.toURL().openStream()) {
         *         Files.copy(in, path);
         *     }
         * </pre>
         *
         * @param   in
         *          the input stream to read from
         * @param   target
         *          the path to the file
         * @param   offset
         *          the offset of the starting location of the input stream
         * @param   length
         *          the length to copy
         * @param   options
         *          options specifying how the copy should be done
         *
         * @return  the number of bytes read or written
         *
         * @throws  IOException
         *          if an I/O error occurs when reading or writing
         * @throws FileAlreadyExistsException
         *          if the target file exists but cannot be replaced because the
         *          {@code REPLACE_EXISTING} option is not specified <i>(optional
         *          specific exception)</i>
         * @throws DirectoryNotEmptyException
         *          the {@code REPLACE_EXISTING} option is specified but the file
         *          cannot be replaced because it is a non-empty directory
         *          <i>(optional specific exception)</i>     *
         * @throws  UnsupportedOperationException
         *          if {@code options} contains a copy option that is not supported
         * @throws  SecurityException
         *          In the case of the default provider, and a security manager is
         *          installed, the {@link SecurityManager#checkWrite(String) checkWrite}
         *          method is invoked to check write access to the file. Where the
         *          {@code REPLACE_EXISTING} option is specified, the security
         *          manager's {@link SecurityManager#checkDelete(String) checkDelete}
         *          method is invoked to check that an existing file can be deleted.
         */
        public static long copy(InputStream in, Path target, long offset, long length, CopyOption... options)
                throws IOException
        {
            // ensure not null before opening file
            Objects.requireNonNull(in);

            // check for REPLACE_EXISTING
            boolean replaceExisting = false;
            for (CopyOption opt: options) {
                if (opt == StandardCopyOption.REPLACE_EXISTING) {
                    replaceExisting = true;
                } else {
                    if (opt == null) {
                        throw new NullPointerException("options contains 'null'");
                    }  else {
                        throw new UnsupportedOperationException(opt + " not supported");
                    }
                }
            }

            // attempt to delete an existing file
            SecurityException se = null;
            if (replaceExisting) {
                try {
                    Files.deleteIfExists(target);
                } catch (SecurityException x) {
                    se = x;
                }
            }

            // attempt to create target file. If it fails with
            // FileAlreadyExistsException then it may be because the security
            // manager prevented us from deleting the file, in which case we just
            // throw the SecurityException.
            OutputStream ostream;
            try {
                ostream = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE);
            } catch (FileAlreadyExistsException x) {
                if (se != null)
                    throw se;
                // someone else won the race and created the file
                throw x;
            }

            // do the copy
            try (OutputStream out = ostream) {
                return copy(in, offset, length, out);
            }
        }
    }
}
