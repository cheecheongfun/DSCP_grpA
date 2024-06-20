package sg.edu.np.mad.greencycle.Forum;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import me.relex.circleindicator.CircleIndicator;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;


public class FullPostActivity extends AppCompatActivity {

    TextView postTitle, postContent, likeCount, postdate, authorDisplayName;
    ViewPager postImagePager;
    CircleIndicator indicator;
    ImageButton likeButton, post_comment_button,back;


    ImageView authorProfilePicture;
    FirebaseFirestore firestoreDb;
    DatabaseReference realtimeDb;
    User user;

    Post post;

    EditText commentInput;
    Button postCommentButton;
    RecyclerView commentsRecyclerView;

    List<Post> postList;

    CommentsAdapter commentsAdapter;
    List<Comment> commentsList = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_post);

        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);
        likeCount = findViewById(R.id.likeCount);
        postdate = findViewById(R.id.postdate);
        authorDisplayName = findViewById(R.id.authorDisplayName);
        authorProfilePicture = findViewById(R.id.authorProfilePicture);
        likeButton = findViewById(R.id.likeButton);
        postImagePager = findViewById(R.id.postImagePager);
        indicator = findViewById(R.id.indicator);
        commentInput = findViewById(R.id.comment_input);
        postCommentButton = findViewById(R.id.comment);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        back = findViewById(R.id.backButton);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });





        firestoreDb = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        post = intent.getParcelableExtra("post");
        user = intent.getParcelableExtra("user");

        Log.v("check", post.getId() + user.getUsername());


        postTitle.setText(post.getTitle());
        postContent.setText(post.getContent());

        commentsAdapter = new CommentsAdapter(commentsList,user,post,this);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);

        loadComments();

        if (post.getUser() != null) {
            fetchUserProfile(post.getUser());
        } else {
            authorDisplayName.setText("Unknown");
            authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
        }

        String postAge = calculatePostAge(post.getTimestamp());
        postdate.setText(postAge);

        List<String> likedBy = post.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
            post.setLikedBy(likedBy);
        }
        likeCount.setText(String.valueOf(likedBy.size()));
        updateLikeButton(likedBy.contains(user.getUsername()));

        List<String> imageUrls = post.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            postImagePager.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.VISIBLE);

            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(this, imageUrls, position1 -> {
                Uri currentImageUri = Uri.parse(imageUrls.get(position1));
                showFullImageDialog(this, currentImageUri);
            });

            postImagePager.setAdapter(imagePagerAdapter);
            indicator.setViewPager(postImagePager);
            imagePagerAdapter.registerDataSetObserver(indicator.getDataSetObserver());

        } else {
            postImagePager.setVisibility(View.GONE);
            indicator.setVisibility(View.GONE);
        }

        AtomicReference<ArrayList<String>> modifiedLikedByRef = new AtomicReference<>(new ArrayList<>(likedBy));

        likeButton.setOnClickListener(v -> {
            ArrayList<String> modifiedLikedBy = modifiedLikedByRef.get();

            // Check if the current user has already liked the post
            boolean isLiked = modifiedLikedBy.contains(user.getUsername());
            if (isLiked) {
                modifiedLikedBy.remove(user.getUsername());
            } else {
                modifiedLikedBy.add(user.getUsername());
            }

            // Update Firestore
            firestoreDb.collection("Post").document("posts").collection("posts").document(post.getId())
                    .update("likedBy", modifiedLikedBy)
                    .addOnFailureListener(e -> Log.e("PostAdapter", "Failed to update likes", e));

            // Update the UI
            updateLikeButton(!isLiked);
            likeCount.setText(String.valueOf(modifiedLikedBy.size()));
        });

        postCommentButton.setOnClickListener(v -> postComment());


    }

    private void fetchUserProfile(String username) {
        if (username == null || username.isEmpty()) {
            authorDisplayName.setText("Unknown");
            authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
            return;
        }

        // Fetch display name from Realtime Database
        realtimeDb.child("users").child(username).child("displayname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.getValue(String.class);
                    authorDisplayName.setText(displayName != null ? displayName : "Unknown");
                } else {
                    authorDisplayName.setText("Unknown");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RealtimeDB", "Error fetching display name", error.toException());
                authorDisplayName.setText("Unknown");
            }
        });

        // Fetch profile picture from Firestore
        firestoreDb.collection("Users").document(username)
                .collection("Profile Picture").document("Profile Image ID")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("imageUrl")) {
                        String profilePicture = documentSnapshot.getString("imageUrl");
                        Glide.with(this)
                                .load(profilePicture)
                                .placeholder(R.drawable.green_cycle_icon)
                                .error(R.drawable.green_cycle_icon)
                                .into(authorProfilePicture);
                    } else {
                        authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting profile picture", e);
                    authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
                });
    }


    private void updateLikeButton(boolean isLiked) {
        likeButton.setImageResource(isLiked ? R.drawable.heart_svgrepo_com__1_ : R.drawable.heart_svgrepo_com);
    }

    private void showFullImageDialog(Context context, Uri imageUri) {
        Dialog fullImageDialog = new Dialog(context);
        fullImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fullImageDialog.setContentView(R.layout.full_image_dialog);

        ImageView fullImageView = fullImageDialog.findViewById(R.id.fullImageView);
        Glide.with(context).load(imageUri).into(fullImageView);

        fullImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fullImageDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fullImageDialog.show();

        fullImageView.setOnClickListener(v -> fullImageDialog.dismiss());
    }


    private String calculatePostAge(Timestamp postTime) {
        Date postDate = postTime.toDate();
        long delta = new Date().getTime() - postDate.getTime();
        long deltaSeconds = delta / 1000;
        long deltaMinutes = deltaSeconds / 60;
        long deltaHours = deltaMinutes / 60;
        long deltaDays = deltaHours / 24;
        long deltaWeeks = deltaDays / 7;

        if (deltaDays > 30) {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postDate);
        } else if (deltaWeeks >= 1) {
            return deltaWeeks + " weeks ago";
        } else if (deltaDays > 0) {
            return deltaDays + " days ago";
        } else if (deltaHours > 0) {
            return deltaHours + " hours ago";
        } else if (deltaMinutes > 0) {
            return deltaMinutes + " minutes ago";
        } else {
            return deltaSeconds + " seconds ago";
        }
    }
    private void postComment() {
        String commentText = commentInput.getText().toString();
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the comment object
        Comment comment = new Comment();
        comment.setDisplay(user.getDisplayname());
        comment.setAuthor(user.getUsername());
        comment.setContent(commentText);
        comment.setTimestamp(new Timestamp(new Date()));  // Capture the timestamp at the time of posting
        comment.setPostId(post.getId());

        // Add the comment to Firestore
        firestoreDb.collection("Post")
                .document("posts")
                .collection("posts")
                .document(post.getId())
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(docRef -> {
                    comment.setId(docRef.getId());  // Set the ID for the newly created comment
                    commentsList.add(comment); // Add the comment to the local list
                    commentsAdapter.notifyItemInserted(commentsList.size() - 1);  // Notify the adapter of the new item
                    commentInput.setText(""); // Clear the input after posting
                    Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error adding comment", e);
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                });
    }




    private void loadComments() {
        firestoreDb.collection("Post")
                .document("posts")
                .collection("posts")
                .document(post.getId())
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Firebase", "Failed to fetch comments", error);
                            return;
                        }

                        commentsList.clear(); // Clear existing data to avoid duplicates
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                comment.setId(doc.getId()); // Set the ID from the document
                                commentsList.add(comment);
                            }
                        }
                        commentsAdapter.notifyDataSetChanged(); // Notify the adapter to refresh the views
                    }
                });
    }

    // Method to handle posting a reply
    public void postReply(Comment parentComment, String replyText) {
        Reply reply = new Reply();
        reply.setAuthor(user.getUsername());
        reply.setDisplay(user.getDisplayname());
        reply.setContent("@" + parentComment.getAuthor() + " " + replyText);
        reply.setTimestamp(new Timestamp(new Date()));

        firestoreDb.collection("Post")
                .document(post.getId())
                .collection("comments")
                .document(parentComment.getId())
                .collection("replies")
                .add(reply)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Reply", "Reply added successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.e("Reply", "Failed to add reply", e);
                });
    }

    // Method to show reply input dialog
    public void showReplyInput(Comment parentComment) {
        Dialog replyDialog = new Dialog(this);
        replyDialog.setContentView(R.layout.dialog_reply_input);

        EditText replyInput = replyDialog.findViewById(R.id.reply_input);
        Button postReplyButton = replyDialog.findViewById(R.id.post_reply_button);

        postReplyButton.setOnClickListener(v -> {
            String replyText = replyInput.getText().toString();
            if (!replyText.isEmpty()) {
                postReply(parentComment, replyText);
                replyDialog.dismiss();
            }
        });

        replyDialog.show();
    }





}






