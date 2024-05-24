package sg.edu.np.mad.greencycle.Fragments.Resources;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import sg.edu.np.mad.greencycle.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResourcesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourcesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ResourceAdapter adapter;
    private List<Resource> resourceList;
    private List<Resource> filteredResourceList;
    private TextView noResource;
    private SearchView searchView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ResourcesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResourcesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResourcesFragment newInstance(String param1, String param2) {
        ResourcesFragment fragment = new ResourcesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_resources, container, false);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resources, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        searchView = view.findViewById(R.id.search);
        noResource = view.findViewById(R.id.noResourceTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        resourceList = new ArrayList<>();
        filteredResourceList = new ArrayList<>();
        adapter = new ResourceAdapter(filteredResourceList,getContext());
        recyclerView.setAdapter(adapter);

        // Set up search functionality

        // Set initial data
        setData();
        searchView.setQueryHint("Search Resource");
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
        return view;

    }


    private void setData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Resource");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Resource> newResourceList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String title = snapshot.child("title").getValue(String.class);
                    String info = snapshot.child("info").getValue(String.class);
                    String link = snapshot.child("link").getValue(String.class);
                    String url = snapshot.child("image").getValue(String.class);
                    newResourceList.add(new Resource(newResourceList.size() + 1,info, title, link, url));
                }

                resourceList.addAll(newResourceList);
                filteredResourceList.addAll(newResourceList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors here
            }
        });
    }


    private void filter(String text) {
        filteredResourceList.clear();
        if (text.isEmpty()) {
            filteredResourceList.addAll(resourceList);

        } else {
            text = text.toLowerCase();
            for (Resource item : resourceList) {
                if (item.getResourcetitle().toLowerCase().contains(text)) {
                    filteredResourceList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }


    
}