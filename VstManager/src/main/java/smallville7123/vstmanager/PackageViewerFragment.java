package smallville7123.vstmanager;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import dalvik.system.DexFile;
import smallville7123.vstmanager.core.VST;

public final class PackageViewerFragment extends Fragment {
    private static final String EXTRA_TARGET_PACKAGE_NAME = "target_package_name";
    private static final String EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY = "package_name_fragment_activity";
    private static final String EXTRA_TARGET_PACKAGE_LIST = "target_package_list";
    private final PackageViewerAdapter.Listener mAdapterListener = new PackageViewerAdapter.Listener() {
        @Override
        public void onItemClick(@NonNull ObjectInfo objectInfo) {
            if (isVisible()) {
                if (objectInfo.getType().isClass()) {
                    // TODO: show class properties.
                } else {
                    if (objectInfo.getType() == ObjectInfo.Type.APPLICATION) {
                        manager.processObject(objectInfo);
                    } else {
                        if (getFragmentManager() != null) {
                            getFragmentManager().beginTransaction()
                                    .replace(android.R.id.content, newInstance(objectInfo.getName()))
                                    .addToBackStack("tag")
                                    .setTransition(FragmentTransaction.TRANSIT_NONE)
                                    .commit();
                        }
                    }
                }
            }
        }
    };

    @Nullable
    RecyclerView mRecyclerView = null;

    @Nullable
    TextView mTextView = null;

    @Nullable
    FragmentActivity activity = null;

    @Nullable
    VstManager manager = null;

    public PackageViewerFragment(FragmentActivity mOrigin, VstManager vstManager) {
        activity = mOrigin;
        manager = vstManager;
        if (mOrigin instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) mOrigin).getSupportActionBar();
            if (actionBar != null) {
                final Bundle bundle = new Bundle();
                bundle.putString(EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY, actionBar.getTitle().toString());
                setArguments(bundle);
            }
        } else {
            android.app.ActionBar actionBar = mOrigin.getActionBar();
            if (actionBar != null) {
                final Bundle bundle = new Bundle();
                bundle.putString(EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY, actionBar.getTitle().toString());
                setArguments(bundle);
            }
        }
    }

    public PackageViewerFragment newInstance(@NonNull final String targetPackageName) {
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TARGET_PACKAGE_NAME, targetPackageName);
        final PackageViewerFragment fragment = new PackageViewerFragment(activity, manager);
        fragment.setArguments(bundle);
        return fragment;
    }

    public PackageViewerFragment newInstance(@NonNull final List<String> targetPackageList) {
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(EXTRA_TARGET_PACKAGE_LIST, new ArrayList<>(targetPackageList));
        final PackageViewerFragment fragment = new PackageViewerFragment(activity, manager);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_package_viewer, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mTextView = view.findViewById(R.id.error_text);

        setupRecyclerView();
        setupParentActivityTitle();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupData();
    }

    @SuppressWarnings("ConstantConditions")
    private void setupRecyclerView() {
        PackageViewerAdapter adapter = new PackageViewerAdapter(mAdapterListener);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @NonNull
    private List<ObjectInfo> getTargetPackageList() {
        if (manager.mInstalledApplications.isEmpty()) {
            return null;
        } else {
            final List<ObjectInfo> retValues = new ArrayList<>();

//            for (ApplicationInfo installedApplication : manager.mInstalledApplications) {
//                retValues.add(new ObjectInfo(installedApplication, manager.mPackageManager));
//            }

            for (VST vst : manager.mVstHost.getVstList()) {
                retValues.add(new ObjectInfo(vst));
            }

            return retValues;
        }
    }

    private List<ObjectInfo> getClassListFromPackage(String packageName) {
        final List<ObjectInfo> list = new ArrayList<>();
        if (getContext() == null) return list;

        try {
            final String packageCodePath = getContext().getPackageCodePath();
            final DexFile df = new DexFile(packageCodePath);
            for (Enumeration<String> iterator = df.entries(); iterator.hasMoreElements(); ) {
                final String fullClassPath = iterator.nextElement();
                final ObjectInfo additionalObject;
                final String className;
                if (TextUtils.isEmpty(packageName)) {
                    className = fullClassPath;
                } else {
                    if (!fullClassPath.startsWith(packageName) || fullClassPath.contains("$"))
                        continue;
                    className = fullClassPath.replace(packageName + ".", "");
                }

                final String[] splitClassName = className.split(Pattern.quote("."), 0);
                if (splitClassName.length == 1) {
                    // file
                    additionalObject = new ObjectInfo(className, packageName);
                } else if (splitClassName.length > 1) {
                    // directory
                    additionalObject = new ObjectInfo(splitClassName[0], packageName, ObjectInfo.Type.DIRECTORY);
                } else {
                    continue;
                }
                if (!list.contains(additionalObject)) list.add(additionalObject);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void setupData() {
        if (mRecyclerView == null || mTextView == null) return;

        List<ObjectInfo> displayList = getTargetPackageList();

        mRecyclerView.setVisibility(displayList.isEmpty() ? View.GONE : View.VISIBLE);
        mTextView.setVisibility(displayList.isEmpty() ? View.VISIBLE : View.GONE);

        PackageViewerAdapter adapter = (PackageViewerAdapter) mRecyclerView.getAdapter();
        adapter.setItems(displayList);
    }

    private void setupParentActivityTitle() {
        final String packageName = getTitle();
        if (getActivity() instanceof AppCompatActivity) {
            final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) actionBar.setTitle(packageName);
        }
    }

    @NonNull
    private String getTitle() {
        if (getArguments() == null) {
            return "";
        }
        return getArguments().getString(EXTRA_TARGET_PACKAGE_NAME, getArguments().getString(EXTRA_TARGET_PACKAGE_NAME_FRAGMENT_ACTIVITY, ""));
    }

    @NonNull
    private String getPackageName() {
        if (getArguments() == null) {
            return "";
        }
        return getArguments().getString(EXTRA_TARGET_PACKAGE_NAME, "");
    }

    @NonNull
    private List<String> getPackageList() {
        if (getArguments() == null) {
            return new ArrayList<>();
        }

        List<String> ret = getArguments().getStringArrayList(EXTRA_TARGET_PACKAGE_LIST);

        if (ret == null) {
            return new ArrayList<>();
        }

        return ret;
    }
}
