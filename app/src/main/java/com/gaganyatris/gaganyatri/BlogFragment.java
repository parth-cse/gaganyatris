package com.gaganyatris.gaganyatri;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.gaganyatris.gaganyatri.utils.LoadingDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BlogFragment extends Fragment {

    private LinearLayout blogContainer;
    private LinearLayout noBlogsLayout;
    private EditText searchEditText;
    private List<DocumentSnapshot> blogList = new ArrayList<>();
    private FirebaseFirestore firestoreDB;
    private LoadingDialog loadingDialog;

    public BlogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, container, false);
        initialize(view);
        setupListeners();
        fetchBlogs();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBlogs();
    }

    private void initialize(View view) {
        blogContainer = view.findViewById(R.id.blogContainer);
        noBlogsLayout = view.findViewById(R.id.noBlogs);
        searchEditText = view.findViewById(R.id.etSearch);
        firestoreDB = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(requireContext());
    }

    private void setupListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBlogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchBlogs() {
        loadingDialog.setMessage("Please Wait...");
        loadingDialog.show();
        firestoreDB.collection("blogs").get().addOnCompleteListener(task -> {
            loadingDialog.dismiss();
            if (task.isSuccessful() && task.getResult() != null) {
                blogList = task.getResult().getDocuments();
                updateUI(blogList);
            } else {
                updateUI(null);
            }
        });
    }

    private void updateUI(List<DocumentSnapshot> blogs) {
        if (blogs == null || blogs.isEmpty()) {
            noBlogsLayout.setVisibility(View.VISIBLE);
            blogContainer.setVisibility(View.GONE);
        } else {
            noBlogsLayout.setVisibility(View.GONE);
            blogContainer.setVisibility(View.VISIBLE);
            displayBlogs(blogs);
        }
    }

    private void displayBlogs(List<DocumentSnapshot> blogs) {
        blogContainer.removeAllViews();
        for (DocumentSnapshot blog : blogs) {
            View blogCard = LayoutInflater.from(getContext()).inflate(R.layout.blog_card, blogContainer, false);
            TextView titleTextView = blogCard.findViewById(R.id.title);
            TextView dateTextView = blogCard.findViewById(R.id.date);
            TextView cityTextView = blogCard.findViewById(R.id.city);
            ImageView blogImageView = blogCard.findViewById(R.id.blog_image);

            titleTextView.setText(blog.getString("title"));
            cityTextView.setText(blog.getString("city"));

            Timestamp timestamp = blog.getTimestamp("date");
            dateTextView.setText(formatDate(timestamp));

            blogImageView.setImageResource(getCityImageResource(blog.getString("city")));
            blogContainer.addView(blogCard);
        }
    }

    private String formatDate(Timestamp timestamp) {
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return dateFormat.format(date);
        }
        return "Date Unavailable";
    }

    private int getCityImageResource(String city) {
        if (city == null) return R.drawable.default_image;
        String formattedCityName = city.toLowerCase().replace(" ", "_");
        int resId = getResources().getIdentifier(formattedCityName, "drawable", requireContext().getPackageName());
        return resId != 0 ? resId : R.drawable.default_image;
    }

    private void filterBlogs(String searchText) {
        List<DocumentSnapshot> filteredList = blogList.stream()
                .filter(blog -> {
                    String title = blog.getString("title");
                    String city = blog.getString("city");
                    return (title != null && title.toLowerCase().contains(searchText.toLowerCase())) ||
                            (city != null && city.toLowerCase().contains(searchText.toLowerCase()));
                })
                .collect(Collectors.toList());
        displayBlogs(filteredList);
    }
}