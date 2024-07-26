package sg.edu.np.mad.greencycle.Forum;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final List<Comment> comments;
    private final FirebaseFirestore firestoreDb;
    private final Post post;
    private final User user;
    private final Context context;

    public CommentsAdapter(List<Comment> comments, User currentUser, Post currentPost, Context context) {
        this.comments = comments;
        this.user = currentUser;
        this.post = currentPost;
        this.context = context;

        this.firestoreDb = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.author.setText(comment.getDisplay());
        holder.content.setText(comment.getContent());

        String postAge = calculatePostAge(comment.getTimestamp());
        holder.postDate.setText(postAge);

        List<String> likedBy = comment.getLikedBy();
        if (likedBy == null) {
            likedBy = new ArrayList<>();
            comment.setLikedBy(likedBy);
        }
        holder.likeCount.setText(String.valueOf(likedBy.size()));
        updateLikeButton(holder, likedBy.contains(user.getUsername()));

        AtomicReference<ArrayList<String>> modifiedLikedByRef = new AtomicReference<>(new ArrayList<>(likedBy));
        holder.likeButton.setOnClickListener(v -> {
            ArrayList<String> modifiedLikedBy = modifiedLikedByRef.get();

            boolean isLiked = modifiedLikedBy.contains(user.getUsername());
            if (isLiked) {
                modifiedLikedBy.remove(user.getUsername());
            } else {
                modifiedLikedBy.add(user.getUsername());
            }

            if (post != null && post.getId() != null && comment != null && comment.getId() != null) {
                firestoreDb.collection("Post")
                        .document("posts")
                        .collection("posts")
                        .document(post.getId())
                        .collection("comments")
                        .document(comment.getId())
                        .update("likedBy", modifiedLikedBy)
                        .addOnFailureListener(e -> Log.e("PostAdapter", "Failed to update likes", e));
            } else {
                Log.e("CommentsAdapter", "Post ID or Comment ID is null");
            }

            updateLikeButton(holder, !isLiked);
            holder.likeCount.setText(String.valueOf(modifiedLikedBy.size()));
        });

        holder.repliesText.setOnClickListener(v -> showAddReplyDialog(comment));

        // Set long click listener for deleting comment
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getAuthor().equals(user.getUsername())) {
                showDeleteCommentDialog(comment);
                return true;
            }
            return false;
        });

        loadReplies(comment, holder);
    }

    private void showDeleteCommentDialog(Comment comment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Comment");
        builder.setMessage("Are you sure you want to delete this comment?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteComment(comment));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteComment(Comment comment) {
        if (post != null && post.getId() != null && comment != null && comment.getId() != null) {
            firestoreDb.collection("Post")
                    .document("posts")
                    .collection("posts")
                    .document(post.getId())
                    .collection("comments")
                    .document(comment.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        comments.remove(comment);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Log.e("CommentsAdapter", "Failed to delete comment", e));
        } else {
            Log.e("CommentsAdapter", "Post ID or Comment ID is null");
        }
    }



    private void showAddReplyDialog(Comment parentComment) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_reply_input, null);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();

        EditText replyInput = dialogView.findViewById(R.id.reply_input);
        replyInput.setText("@" + parentComment.getDisplay() + " ");
        replyInput.setSelection(replyInput.getText().length());

        Button postReplyButton = dialogView.findViewById(R.id.post_reply_button);
        postReplyButton.setOnClickListener(v -> {
            String replyText = replyInput.getText().toString().trim();
            if (!replyText.isEmpty()) {
                postReplyButton.setEnabled(false);  // Disable the button to prevent multiple posts
                Reply newReply = new Reply();
                newReply.setAuthor(user.getUsername());
                newReply.setDisplay(user.getDisplayname()); // Assuming you have a method to get display name
                newReply.setContent(replyText);
                newReply.setTimestamp(Timestamp.now());
                newReply.setCommentId(parentComment.getId()); // Set the parent comment ID

                DocumentReference commentRef = firestoreDb.collection("Post")
                        .document("posts")
                        .collection("posts")
                        .document(post.getId())
                        .collection("comments")
                        .document(parentComment.getId());

                // Add the new reply
                commentRef.collection("replies").add(newReply)
                        .addOnSuccessListener(documentReference -> {
                            newReply.setId(documentReference.getId()); // Set the newly created ID from Firestore to the reply object
                            documentReference.update("id", newReply.getId()); // Update the reply document with its ID

                            // After adding the reply successfully, update the repliedBy field of the parent comment
                            commentRef.update("repliedBy", FieldValue.arrayUnion(user.getUsername()))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("CommentsAdapter", "Updated repliedBy successfully");
                                        bottomSheetDialog.dismiss(); // Dismiss the dialog upon successful operation
                                        postReplyButton.setEnabled(true);  // Re-enable the button
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("CommentsAdapter", "Failed to update repliedBy", e);
                                        postReplyButton.setEnabled(true);  // Re-enable the button on failure
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("CommentsAdapter", "Failed to add reply", e); // Log any errors encountered during the operation
                            postReplyButton.setEnabled(true);  // Re-enable the button on failure
                        });
            } else {
                Toast.makeText(context, "Reply cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void loadReplies(Comment parentComment, ViewHolder holder) {
        firestoreDb.collection("Post")
                .document("posts")
                .collection("posts")
                .document(post.getId())
                .collection("comments")
                .document(parentComment.getId())
                .collection("replies")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("CommentsAdapter", "Failed to load replies", e);
                        return;
                    }

                    List<Reply> replies = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Reply reply = doc.toObject(Reply.class);
                        replies.add(reply);
                    }

                    if (!replies.isEmpty()) {
                        holder.viewRepliesCount.setVisibility(View.VISIBLE);
                        holder.viewRepliesCount.setText("View " + replies.size() + " more replies");
                    } else {
                        holder.viewRepliesCount.setVisibility(View.GONE);
                    }

                    holder.viewRepliesCount.setOnClickListener(v -> {
                        if (holder.repliesRecyclerView.getVisibility() == View.GONE) {
                            holder.repliesRecyclerView.setVisibility(View.VISIBLE);
                            holder.viewRepliesCount.setText("Hide replies");
                        } else {
                            holder.repliesRecyclerView.setVisibility(View.GONE);
                            holder.viewRepliesCount.setText("View " + replies.size() + " more replies");
                        }
                    });

                    RepliesAdapter repliesAdapter = new RepliesAdapter(replies, user);
                    holder.repliesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                    holder.repliesRecyclerView.setAdapter(repliesAdapter);
                });
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView author, content, likeCount, postDate, repliesText,viewRepliesCount;
        ImageButton likeButton;
        Button replyButton;
        RecyclerView repliesRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.comment_author);
            content = itemView.findViewById(R.id.comment_content);
            likeCount = itemView.findViewById(R.id.likeCount);
            likeButton = itemView.findViewById(R.id.likeButton);
            postDate = itemView.findViewById(R.id.comment_date);
            repliesText = itemView.findViewById(R.id.replies_text);
            viewRepliesCount = itemView.findViewById(R.id.view_replies_count);
            repliesRecyclerView = itemView.findViewById(R.id.replies_recycler_view);
        }
    }

    private void updateLikeButton(ViewHolder holder, boolean isLiked) {
        holder.likeButton.setImageResource(isLiked ? R.drawable.like_svgrepo_com__1_ : R.drawable.like_svgrepo_com);
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
}
