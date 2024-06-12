package sg.edu.np.mad.greencycle.Forum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.greencycle.Classes.User;
import sg.edu.np.mad.greencycle.NPKvalue.npk_value;
import sg.edu.np.mad.greencycle.R;

public class Forum extends AppCompatActivity {

    User user;

    ImageButton back,newpost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forum);
        Intent receivingEnd = getIntent();
        user = receivingEnd.getParcelableExtra("user");
        Log.v("user",user.getUsername());

        back = findViewById(R.id.backButton);
        newpost = findViewById(R.id.addpost);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        newpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Forum.this, NewPost.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });


    }
}