package smallville7123.todo;

import org.jetbrains.annotations.Contract;

public class TODO {
    /**
     * exception
     */
    @Contract(" -> fail")
    public static final void TODO() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
