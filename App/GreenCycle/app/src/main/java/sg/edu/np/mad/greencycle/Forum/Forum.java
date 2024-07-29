package sg.edu.np.mad.greencycle.Forum;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.database.Cursor;
import android.widget.TextView;

public class Forum extends AppCompatActivity {

    private User user;
    private HashSet<Button> selectedButtons = new HashSet<>();


    private LinearLayout tagsContainer;
    private ImageButton back, newpost;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SearchView searchView;
    private SimpleCursorAdapter suggestionsAdapter;

    private TextView top,hot,newPosts,all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

        // Initialize UI components and listeners
        initViews();
        setupTagButtons();
        setupSearchView();
        fetchPosts("all");  // Fetch all posts initially

        back.setOnClickListener(view -> finish());
        newpost.setOnClickListener(view -> {
            Intent intent = new Intent(Forum.this, NewPost.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });
    }

    private void initViews() {
        back = findViewById(R.id.backButton);
        newpost = findViewById(R.id.addpost);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.search);
        tagsContainer = findViewById(R.id.tagsContainer);
        user = getIntent().getParcelableExtra("user");

        top = findViewById(R.id.Top);
        hot = findViewById(R.id.Hot);
        newPosts = findViewById(R.id.New);
        all = findViewById(R.id.All);

        // Setup click listeners
        top.setOnClickListener(v -> {
            fetchPosts("top");
            updateButtonSelection(top);
        });
        hot.setOnClickListener(v -> {
            fetchPosts("hot");
            updateButtonSelection(hot);
        });
        newPosts.setOnClickListener(v -> {
            fetchPosts("new");
            updateButtonSelection(newPosts);
        });
        all.setOnClickListener(v -> {
            fetchPosts("all");
            updateButtonSelection(all);
        });

        // Fetch all posts initially and update UI
        fetchPosts("all");
        updateButtonSelection(all);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postList, this, user);
        recyclerView.setAdapter(postAdapter);
    }



    private void setupTagButtons() {
        String[] baseTags = {"#Vermicompost", "#Wormhealth", "#Compost", "#Feeding", "#Wormtype", "#Feed"};
        tagsContainer = findViewById(R.id.tagsContainer);

        int buttonHeight = 100; // Set the desired height of the buttons, in pixels.
        Drawable selectedBackground = getResources().getDrawable(R.drawable.selected, null);
        Drawable normalBackground = getResources().getDrawable(R.drawable.rounded_button, null);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                buttonHeight
        );
        layoutParams.setMargins(10, 10, 10, 10);

        for (String tag : baseTags) {
            Button tagButton = new Button(this);
            tagButton.setText(tag);
            tagButton.setBackground(normalBackground);
            tagButton.setLayoutParams(layoutParams);
            tagButton.setTextColor(Color.WHITE);
            tagButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tagButton.setPadding(20, 10, 20, 10);
            tagButton.setAllCaps(false);

            // Set onClickListener to toggle selection state
            tagButton.setOnClickListener(v -> {
                if (selectedButtons.contains(tagButton)) {
                    tagButton.setBackground(normalBackground);
                    selectedButtons.remove(tagButton);
                } else {
                    tagButton.setBackground(selectedBackground);
                    selectedButtons.add(tagButton);
                }
                fetchPostsBasedOnSelection();
            });

            tagsContainer.addView(tagButton);
        }
    }

    private void fetchPostsBasedOnSelection() {
        if (selectedButtons.isEmpty()) {
            fetchPosts("all");  // Fetch all posts if no buttons are selected
        } else {
            for (Button selectedButton : selectedButtons) {
                String tag = selectedButton.getText().toString();
                fetchPostsByTag(tag);  // Fetch posts for each selected tag
            }
        }
    }

    private void fetchPostsByTag(String tag) {
        // Execute a new query based on the selected tag
        Query query = db.collection("Post").document("posts").collection("posts")
                .whereArrayContains("tags", tag)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error);
                    return;
                }

                postList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Post post = doc.toObject(Post.class);
                    post.setId(doc.getId());
                    if (post.getLikedBy() == null) {
                        post.setLikedBy(new ArrayList<>());
                    }
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }
        });
    }


    private void updateButtonSelection(TextView selectedButton) {
        top.setTextColor(Color.DKGRAY);
        hot.setTextColor(Color.DKGRAY);
        newPosts.setTextColor(Color.DKGRAY);
        all.setTextColor(Color.DKGRAY);

        selectedButton.setTextColor(getResources().getColor(R.color.mid_green));  // Highlight selected button
    }

    private void setupSearchView() {
        final String[] from = new String[]{"postTitle"};
        final int[] to = new int[]{android.R.id.text1};
        suggestionsAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1,
                null,
                from,
                to,
                0);
        searchView.setSuggestionsAdapter(suggestionsAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    filterPosts(""); // Display all posts if the search query is empty
                } else {
                    showSuggestions(newText);
                }
                return true;
            }
        });


        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) suggestionsAdapter.getItem(position);
                int itemIndex = cursor.getColumnIndex("_id");
                long postId = cursor.getLong(itemIndex);
                if (postId == -1) { // Check if "See More..." was clicked
                    filterPosts(searchView.getQuery().toString());
                    searchView.clearFocus(); // Close the suggestion dropdown
                } else {
                    Post selectedPost = findPostById(postId);
                    openFullPostActivity(selectedPost);
                }
                return true;
            }
        });

    }


    private Post findPostById(long postId) {
        for (Post post : postList) {
            if (post.getId().hashCode() == postId) {
                return post;
            }
        }
        return null; // Or handle error scenario
    }

    private void openFullPostActivity(Post post) {
        Intent intent = new Intent(this, FullPostActivity.class);
        intent.putExtra("post", post);
        intent.putExtra("user", user);
        startActivity(intent);
    }


    private void filterPosts(String query) {
        List<Post> filteredPosts = new ArrayList<>();
        if (query.isEmpty()) {
            filteredPosts.addAll(postList); // Add all posts if the query is empty
        } else {
            for (Post post : postList) {
                if (post.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredPosts.add(post);
                }
            }
        }
        postAdapter.updateData(filteredPosts); // Update adapter with filtered list
    }






    private void showSuggestions(String query) {
        MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "postTitle"});
        if (query.isEmpty()) {
            for (Post post : postList) { // Add all posts if the query is empty
                cursor.addRow(new Object[]{post.getId().hashCode(), post.getTitle()});
            }
        } else {
            String[] queryWords = query.toLowerCase().split("\\s+"); // Split the query on one or more spaces
            int count = 0;
            for (Post post : postList) {
                String postTitle = post.getTitle().toLowerCase();
                boolean matchesAnyWord = false;
                for (String word : queryWords) {
                    if (postTitle.contains(word)) {
                        matchesAnyWord = true;
                        break; // Break as soon as one word matches
                    }
                }
                if (matchesAnyWord) {
                    cursor.addRow(new Object[]{post.getId().hashCode(), post.getTitle()});
                    count++;
                    if (count == 5) break; // Limit to 5 suggestions
                }
            }
            if (count >= 5) {
                cursor.addRow(new Object[]{-1, "See More..."}); // Add a special "See More" item if there are many matches
            }
        }
        suggestionsAdapter.changeCursor(cursor);
    }




    private void fetchPosts(String criteria) {
        Query query;
        switch (criteria) {
            case "top":
                query = db.collection("Post").document("posts").collection("posts")
                        .orderBy("likeCount", Query.Direction.DESCENDING);
                break;
            case "hot":
                query = db.collection("Post").document("posts").collection("posts")
                        .orderBy("commentCount", Query.Direction.DESCENDING);
                break;
            case "new":
                query = db.collection("Post").document("posts").collection("posts")
                        .orderBy("timestamp", Query.Direction.DESCENDING);
                break;
            default: // "all" or no selection
                query = db.collection("Post").document("posts").collection("posts")
                        .orderBy("timestamp", Query.Direction.ASCENDING);
                break;
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error);
                    return;
                }

                postList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Post post = doc.toObject(Post.class);
                    post.setId(doc.getId());
                    if (post.getLikedBy() == null) {
                        post.setLikedBy(new ArrayList<>());
                    }
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }
        });
    }

}



