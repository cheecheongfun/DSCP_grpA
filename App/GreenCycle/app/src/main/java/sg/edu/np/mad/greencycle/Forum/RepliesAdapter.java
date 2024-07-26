package sg.edu.np.mad.greencycle.Forum;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.ViewHolder> {

    private final List<Reply> replies;
    private final User currentUser; // Assuming you have a current user object

    public RepliesAdapter(List<Reply> replies, User user) {
        this.replies = replies;
        this.currentUser = user; // Set the current user
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reply_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reply reply = replies.get(position);

        holder.author.setText(reply.getDisplay());
        holder.content.setText(reply.getContent());
        String replyAge = calculateReplyAge(reply.getTimestamp());
        holder.replyDate.setText(replyAge);

        // Set long click listener for delete action
        holder.itemView.setOnLongClickListener(v -> {
            if (reply.getAuthor().equals(currentUser.getUsername())) { // Assuming you have a method to get the current user ID
                showDeleteReplyDialog(holder.itemView.getContext(), position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView author, content, replyDate;

        public ViewHolder(View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.reply_author);
            content = itemView.findViewById(R.id.reply_content);
            replyDate = itemView.findViewById(R.id.reply_date);
        }
    }

    private String calculateReplyAge(Timestamp timestamp) {
        if (timestamp == null) {
            return "Unknown date";
        }

        Date date = timestamp.toDate();
        long delta = new Date().getTime() - date.getTime();
        long deltaSeconds = delta / 1000;
        long deltaMinutes = deltaSeconds / 60;
        long deltaHours = deltaMinutes / 60;
        long deltaDays = deltaHours / 24;
        long deltaWeeks = deltaDays / 7;

        if (deltaDays > 30) {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
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

    private void showDeleteReplyDialog(Context context, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Reply")
                .setMessage("Are you sure you want to delete this reply?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    deleteReply(context, position);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteReply(Context context, int position) {
        Reply reply = replies.get(position); // Assuming `replies` is a list of Reply objects
        // Remove the reply from the local dataset
        replies.remove(position);
        notifyItemRemoved(position);

        // Remove the reply from Firebase Firestore (assuming you have the necessary setup)
        FirebaseFirestore.getInstance()
                .collection("Replies")
                .document(reply.getId()) // Assuming `reply.getId()` gets the document ID of the reply
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Reply deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete reply: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
