package sg.edu.np.mad.greencycle.Forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.R;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.ViewHolder> {

    private final List<Reply> replies;

    public RepliesAdapter(List<Reply> replies, Comment parentComment, User user, Post post, Context context) {
        this.replies = replies;
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
}
