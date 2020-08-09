package name.admitriev.jhelper.ui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * ListModel that allows adding an entire {@code Collection}
 */
public class ParseListModel<T> extends AbstractListModel<T> {
    private final List<T> list = new ArrayList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public T getElementAt(int index) {
        return list.get(index);
    }

    public void removeAll() {
        int size = getSize();
        if (size == 0) {
            return;
        }
        list.clear();
        fireIntervalRemoved(this, 0, size - 1);
    }

    public void addAll(Collection<T> collection) {
        if (collection.isEmpty()) {
            return;
        }
        int size = getSize();
        list.addAll(collection);
        fireIntervalAdded(this, size, getSize() - 1);
    }
}
