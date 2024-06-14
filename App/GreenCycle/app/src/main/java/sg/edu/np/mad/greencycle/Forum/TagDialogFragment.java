package sg.edu.np.mad.greencycle.Forum;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import sg.edu.np.mad.greencycle.R;

public class TagDialogFragment extends BottomSheetDialogFragment {
    private Set<String> selectedTags = new HashSet<>();
    private Set<String> tempSelectedTags = new HashSet<>();
    private GridLayout tagsContainer;
    private TagDialogListener listener;

    public interface TagDialogListener {
        void onTagsSelected(Set<String> tags);
    }

    public void setTagDialogListener(TagDialogListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_dialog, container, false);
        tagsContainer = view.findViewById(R.id.tagsContainer);
        EditText newTagInput = view.findViewById(R.id.newTagInput);
        Button btnAddTag = view.findViewById(R.id.btnAddTag);
        Button btnDone = view.findViewById(R.id.btnDone);
        tempSelectedTags.addAll(selectedTags);
        TextView removeLabels = view.findViewById(R.id.removelabels);
        removeLabels.setOnClickListener(v -> showConfirmationDialog());

        // Initialize tempSelectedTags with the current selected tags
        tempSelectedTags.addAll(selectedTags);

        String[] baseTags = {"#Vermicompost", "#Wormhealth", "#Compost", "#Feeding", "#Wormtype", "#Feed"};
        loadTags();

        btnAddTag.setOnClickListener(v -> {
            String newTag = "#" + newTagInput.getText().toString().trim();
            if (!newTag.isEmpty() && !tempSelectedTags.contains(newTag)) {
                addTag(newTag, true);
                newTagInput.setText("");
            } else {
                Toast.makeText(getContext(), "Invalid or duplicate tag", Toast.LENGTH_SHORT).show();
            }
        });


        btnDone.setOnClickListener(v -> {
            selectedTags.clear();
            selectedTags.addAll(tempSelectedTags);
            if (listener != null) {
                listener.onTagsSelected(selectedTags);
            }
            dismiss();
        });

        return view;
    }

    private void loadTags() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserTags", Context.MODE_PRIVATE);
        Set<String> savedTags = prefs.getStringSet("tags", new HashSet<>());
        // Base tags are predefined and should appear first.
        String[] baseTags = {"#Vermicompost", "#Wormhealth", "#Compost", "#Feeding", "#Wormtype", "#Feed"};
        Set<String> allTags = new LinkedHashSet<>(Arrays.asList(baseTags)); // Use LinkedHashSet to maintain order

        // Add saved custom tags to the end.
        allTags.addAll(savedTags); // This adds all the custom tags after the base tags

        for (String tag : allTags) {
            addTag(tag, false);
        }
    }



    private void addTag(String tag, boolean isNew) {
        MaterialButton button = new MaterialButton(getContext());
        button.setText(tag);
        button.setAllCaps(false);
        button.setCornerRadius(50);
        int padding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        button.setPadding(padding, padding, padding, padding);

        // Set margins and background color for the button
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);

        button.setTextColor(getResources().getColor(R.color.black));
        button.setBackgroundColor(getResources().getColor(R.color.white));

        button.setOnClickListener(v -> {
            boolean isSelected = !button.isSelected();
            button.setSelected(isSelected);
            button.setBackgroundColor(isSelected ? getResources().getColor(R.color.mid_green) : getResources().getColor(R.color.white));
            if (isSelected) {
                tempSelectedTags.add(tag);
            } else {
                tempSelectedTags.remove(tag);
            }

        });

        // Set initial selection state
        if (tempSelectedTags.contains(tag)) {
            button.setSelected(true);
            button.setBackgroundColor(getResources().getColor(R.color.mid_green));
        }

        tagsContainer.addView(button);

        // Notify listener if it's a new tag
        if (isNew) {
            saveTagToPreferences(tag);
        }
    }

    private void saveTagToPreferences(String tag) {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserTags", Context.MODE_PRIVATE);
        Set<String> tags = prefs.getStringSet("tags", new HashSet<>());
        Set<String> newTags = new HashSet<>(tags); // Workaround for SharedPreferences StringSet immutability
        newTags.add(tag);
        prefs.edit().putStringSet("tags", newTags).apply();
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onTagsSelected(selectedTags);
        }
    }
    public void setSelectedTags(Set<String> initialSelectedTags) {
        selectedTags = new HashSet<>(initialSelectedTags); // Initialize with the passed tags
    }


    private void showConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete all custom labels?")
                .setPositiveButton("Yes", (dialog, which) -> deleteCustomTags())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteCustomTags() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserTags", Context.MODE_PRIVATE);
        prefs.edit().remove("tags").apply();  // Remove the tag set

        // Optionally update UI and internal state
        tempSelectedTags.clear();
        selectedTags.clear();
        tagsContainer.removeAllViews();  // Clear the UI of tags
        String[] baseTags = {"#Vermicompost", "#Wormhealth", "#Compost", "#Feeding", "#Wormtype", "#Feed"};
        loadTags();  // Reload the base tags
    }






}
