package sg.edu.np.mad.greencycle.Forum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class Forum extends AppCompatActivity {

    private User user;
    private ImageButton back, newpost;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);

        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        Log.v("user", user.getUsername());

        back = findViewById(R.id.backButton);
        newpost = findViewById(R.id.addpost);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postList, this, user);
        recyclerView.setAdapter(postAdapter);

        back.setOnClickListener(view -> finish());

        newpost.setOnClickListener(view -> {
            Intent intent = new Intent(Forum.this, NewPost.class);
            intent.putExtra("user", user);
            startActivity(intent);
        });

        fetchPosts();
    }

    private void fetchPosts() {
        db.collection("Post")
                .document("posts")
                .collection("posts")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
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
