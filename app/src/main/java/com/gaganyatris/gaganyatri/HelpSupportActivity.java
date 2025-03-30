package com.gaganyatris.gaganyatri;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class HelpSupportActivity extends AppCompatActivity {

    private static final int ANIMATION_DURATION = 300;
    private static final String EMAIL_ADDRESS = "parthjamkhedkarjnec@gmail.com";
    private static final int STATUS_BAR_COLOR = R.color.newStatusBar;

    private LinearLayout optionsLayout;
    private View bgView;
    private ImageButton backBtn;

    // Store question-answer pairs for easier management
    private final Map<Integer, Integer> questionAnswerMap = new HashMap<>();
    private final Map<Integer, Integer> questionArrowMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help_support);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();
        setupOnBackPressed();
    }

    private void initializeViews() {
        optionsLayout = findViewById(R.id.optionsLayout);
        bgView = findViewById(R.id.bg);
        backBtn = findViewById(R.id.backBtn);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, STATUS_BAR_COLOR));

        // Populate the maps for question-answer and question-arrow pairs
        questionAnswerMap.put(R.id.question1_layout, R.id.question1_answer);
        questionAnswerMap.put(R.id.question2_layout, R.id.question2_answer);
        questionAnswerMap.put(R.id.question3_layout, R.id.question3_answer);
        questionAnswerMap.put(R.id.question4_layout, R.id.question4_answer);
        questionAnswerMap.put(R.id.question5_layout, R.id.question5_answer);

        questionArrowMap.put(R.id.question1_layout, R.id.q1_arrow);
        questionArrowMap.put(R.id.question2_layout, R.id.q2_arrow);
        questionArrowMap.put(R.id.question3_layout, R.id.q3_arrow);
        questionArrowMap.put(R.id.question4_layout, R.id.q4_arrow);
        questionArrowMap.put(R.id.question5_layout, R.id.q5_arrow);
    }

    private void setupClickListeners() {
        backBtn.setOnClickListener(view -> finish());

        findViewById(R.id.contactUsBTN).setOnClickListener(v -> {
            bgView.setVisibility(View.VISIBLE);
            optionsLayout.setVisibility(View.VISIBLE);
        });

        bgView.setOnClickListener(v -> hideOptions());

        // Set click listeners for all questions using the map
        for (Map.Entry<Integer, Integer> entry : questionAnswerMap.entrySet()) {
            int questionLayoutId = entry.getKey();
            View questionLayout = findViewById(questionLayoutId);
            if (questionLayout != null) {
                questionLayout.setOnClickListener(v ->
                        toggleQuestion(v, findViewById(questionAnswerMap.get(questionLayoutId)), findViewById(questionArrowMap.get(questionLayoutId))));
            }
        }

        findViewById(R.id.sendEmail).setOnClickListener(v -> sendEmail());
    }

    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (optionsLayout.getVisibility() == View.VISIBLE) {
                    hideOptions();
                } else {
                    finish();
                }
            }
        });
    }

    private void hideOptions() {
        if (optionsLayout.getVisibility() == View.VISIBLE) {
            optionsLayout.setVisibility(View.GONE);
            bgView.setVisibility(View.GONE);
        }
    }

    private void toggleQuestion(View layout, TextView answer, ImageView arrow) {
        boolean isVisible = answer.getVisibility() == View.VISIBLE;

        collapseAllQuestions();

        if (isVisible) {
            animateCollapse(answer);
            rotateArrow(arrow, 180f, 0f);
        } else {
            animateExpand(answer);
            rotateArrow(arrow, 0f, 180f);
        }
    }

    private void rotateArrow(ImageView arrow, float from, float to) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(arrow, "rotation", from, to);
        rotateAnimator.setDuration(ANIMATION_DURATION);
        rotateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimator.start();
    }

    private void animateExpand(TextView answer) {
        answer.measure(View.MeasureSpec.makeMeasureSpec(((View) answer.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int targetHeight = answer.getMeasuredHeight();
        answer.getLayoutParams().height = 0;
        answer.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            answer.getLayoutParams().height = (int) animation.getAnimatedValue();
            answer.requestLayout();
        });

        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }

    private void animateCollapse(TextView answer) {
        int initialHeight = answer.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            answer.getLayoutParams().height = (int) animation.getAnimatedValue();
            answer.requestLayout();
        });

        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                answer.setVisibility(View.GONE);
            }
        });

        animator.start();
    }

    private void collapseAllQuestions() {
        for (int answerId : questionAnswerMap.values()) {
            TextView answer = findViewById(answerId);
            ImageView arrow = findViewById(questionArrowMap.get(getKeyByValue(questionAnswerMap, answerId)));
            collapseIfOpen(answer, arrow);
        }
    }

    private void collapseIfOpen(TextView answer, ImageView arrow) {
        if (answer.getVisibility() == View.VISIBLE) {
            animateCollapse(answer);
            rotateArrow(arrow, 180f, 0f);
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + EMAIL_ADDRESS));

        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(HelpSupportActivity.this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get key from value in a Map
    private static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null; // Or throw an exception if you prefer
    }
}