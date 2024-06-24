package sg.edu.np.mad.greencycle.Forum;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import me.relex.circleindicator.CircleIndicator;
import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final Context context;
    private final FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
    private final DatabaseReference realtimeDb = FirebaseDatabase.getInstance().getReference();
    private final User user;

    public PostAdapter(List<Post> postList, Context context, User user) {
        this.postList = postList;
        this.context = context;
        this.user = user;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return postList.get(position).getId().hashCode();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        final Post post = postList.get(holder.getAdapterPosition());
        holder.postTitle.setText(post.getTitle());
        String content = post.getContent();
        String[] words = content.split("\\s+");
        if (words.length > 100) {
            StringBuilder shortenedContent = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                shortenedContent.append(words[i]).append(" ");
            }
            holder.postContent.setText(shortenedContent.toString().trim() + "...");
            holder.seeMore.setVisibility(View.VISIBLE);
        } else {
            holder.postContent.setText(content);
            holder.seeMore.setVisibility(View.GONE);
        }

        holder.seeMore.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("post", post);
            intent.putExtra("user", user);
            context.startActivity(intent);
        });

        Log.i("POST", post.getId());

        if (post.getUser() != null) {
            fetchUserProfile(post.getUser(), holder);
        } else {
            holder.authorDisplayName.setText("Unknown");
            holder.authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
        }

        String postAge = calculatePostAge(post.getTimestamp());
        holder.postdate.setText(postAge);

        List<String> likedBy = post.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
            post.setLikedBy(likedBy);
        }
        holder.likeCount.setText(String.valueOf(likedBy.size()));
        updateLikeButton(holder, likedBy.contains(user.getUsername()));

        fetchCommentCount(post.getId(), holder);

        if (user.getUsername().equals(post.getUser())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }




        holder.deleteButton.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                showDeleteConfirmationDialog(postList.get(currentPosition), currentPosition);
            }

    });

        List<String> imageUrls = post.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            holder.postImagePager.setVisibility(View.VISIBLE);
            holder.indicator.setVisibility(View.VISIBLE);
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(context, imageUrls, position1 -> {
                Uri currentImageUri = Uri.parse(imageUrls.get(position1));
                showFullImageDialog(context, currentImageUri);
            });
            holder.postImagePager.setAdapter(imagePagerAdapter);
            holder.indicator.setViewPager(holder.postImagePager);
            imagePagerAdapter.registerDataSetObserver(holder.indicator.getDataSetObserver());
        } else {
            holder.postImagePager.setVisibility(View.GONE);
            holder.indicator.setVisibility(View.GONE);
        }

        AtomicReference<ArrayList<String>> modifiedLikedByRef = new AtomicReference<>(new ArrayList<>(likedBy));
        holder.likeButton.setOnClickListener(v -> {
            ArrayList<String> modifiedLikedBy = modifiedLikedByRef.get();
            boolean isLiked = modifiedLikedBy.contains(user.getUsername());
            if (isLiked) {
                modifiedLikedBy.remove(user.getUsername());
            } else {
                modifiedLikedBy.add(user.getUsername());
            }
            firestoreDb.collection("Post").document("posts").collection("posts").document(post.getId())
                    .update("likedBy", modifiedLikedBy)
                    .addOnFailureListener(e -> Log.e("PostAdapter", "Failed to update likes", e));
            updateLikeButton(holder, !isLiked);
            holder.likeCount.setText(String.valueOf(modifiedLikedBy.size()));
        });

        holder.post_comment_button.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullPostActivity.class);
            intent.putExtra("post", post);
            intent.putExtra("user", user);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle, postContent, likeCount, postdate, authorDisplayName, commentCount, seeMore;
        ViewPager postImagePager;
        CircleIndicator indicator;
        ImageButton likeButton, post_comment_button, deleteButton;
        ImageView authorProfilePicture;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitle);
            postContent = itemView.findViewById(R.id.postContent);
            seeMore = itemView.findViewById(R.id.seeMore);
            postImagePager = itemView.findViewById(R.id.postImagePager);
            indicator = itemView.findViewById(R.id.indicator);
            likeCount = itemView.findViewById(R.id.likeCount);
            likeButton = itemView.findViewById(R.id.likeButton);
            postdate = itemView.findViewById(R.id.postdate);
            authorDisplayName = itemView.findViewById(R.id.authorDisplayName);
            authorProfilePicture = itemView.findViewById(R.id.authorProfilePicture);
            post_comment_button = itemView.findViewById(R.id.post_comment_button);
            commentCount = itemView.findViewById(R.id.commentCount);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private String calculatePostAge(Timestamp postTime) {
        if (postTime == null) {
            return "Unknown date";
        }

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

    private void fetchUserProfile(String username, PostViewHolder holder) {
        if (username == null || username.isEmpty()) {
            holder.authorDisplayName.setText("Unknown");
            holder.authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
            return;
        }

        // Fetch display name from Realtime Database
        realtimeDb.child("users").child(username).child("displayname").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.getValue(String.class);
                    holder.authorDisplayName.setText(displayName != null ? displayName : "Unknown");
                } else {
                    holder.authorDisplayName.setText("Unknown");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RealtimeDB", "Error fetching display name", error.toException());
                holder.authorDisplayName.setText("Unknown");
            }
        });

        // Fetch profile picture from Firestore
        firestoreDb.collection("Users").document(username)
                .collection("Profile Picture").document("Profile Image ID")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("imageUrl")) {
                        String profilePicture = documentSnapshot.getString("imageUrl");
                        Glide.with(context)
                                .load(profilePicture)
                                .placeholder(R.drawable.green_cycle_icon)
                                .error(R.drawable.green_cycle_icon)
                                .into(holder.authorProfilePicture);
                    } else {
                        holder.authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting profile picture", e);
                    holder.authorProfilePicture.setImageResource(R.drawable.green_cycle_icon); // Default image
                });
    }

    private void fetchCommentCount(String postId, PostViewHolder holder) {
        firestoreDb.collection("Post").document("posts").collection("posts").document(postId).collection("comments")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("PostAdapter", "Listen failed.", e);
                        return;
                    }
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        holder.commentCount.setText(String.valueOf(querySnapshot.size()));
                    } else {
                        holder.commentCount.setText("0");
                    }
                });
    }

    private void updateLikeButton(PostViewHolder holder, boolean isLiked) {
        holder.likeButton.setImageResource(isLiked ? R.drawable.heart_svgrepo_com__1_ : R.drawable.heart_svgrepo_com);
    }

    private void showDeleteConfirmationDialog(Post post, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    firestoreDb.collection("Post").document("posts").collection("posts").document(post.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("PostAdapter", "Post deleted successfully");
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Log.e("PostAdapter", "Failed to delete post", e));
                })
                .setNegativeButton("No", null)
                .show();
    }



}
