package sg.edu.np.mad.greencycle.Forum;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class PostDiffCallback extends DiffUtil.Callback {
    List<Post> oldList;
    List<Post> newList;

    public PostDiffCallback(List<Post> oldList, List<Post> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        // Return true if the two items represent the same object.
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // Return true if the contents of the items haven't changed.
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
