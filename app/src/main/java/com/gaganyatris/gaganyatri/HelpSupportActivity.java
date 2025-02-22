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

public class HelpSupportActivity extends AppCompatActivity {

    final int statusBarColor = R.color.newStatusBar;
    ImageButton backBtn;

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


        LinearLayout optionsLayout = findViewById(R.id.optionsLayout);
        View bgView = findViewById(R.id.bg); // Initialize the background view

        getWindow().setStatusBarColor(ContextCompat.getColor(this, statusBarColor));

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());

        findViewById(R.id.contactUsBTN).setOnClickListener(v -> {
            bgView.setVisibility(View.VISIBLE);
            optionsLayout.setVisibility(View.VISIBLE);
        });

        findViewById(R.id.bg).setOnClickListener(v -> {
            if (optionsLayout.getVisibility() == View.VISIBLE) {
                optionsLayout.setVisibility(View.GONE);
                bgView.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.question1_layout).setOnClickListener(v ->
                toggleQuestion(v, findViewById(R.id.question1_answer), findViewById(R.id.q1_arrow)));

        findViewById(R.id.question2_layout).setOnClickListener(v ->
                toggleQuestion(v, findViewById(R.id.question2_answer), findViewById(R.id.q2_arrow)));

        findViewById(R.id.question3_layout).setOnClickListener(v ->
                toggleQuestion(v, findViewById(R.id.question3_answer), findViewById(R.id.q3_arrow)));

        findViewById(R.id.question4_layout).setOnClickListener(v ->
                toggleQuestion(v, findViewById(R.id.question4_answer), findViewById(R.id.q4_arrow)));

        findViewById(R.id.question5_layout).setOnClickListener(v ->
                toggleQuestion(v, findViewById(R.id.question5_answer), findViewById(R.id.q5_arrow)));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (optionsLayout.getVisibility() == View.VISIBLE) {
                    optionsLayout.setVisibility(View.GONE);
                    bgView.setVisibility(View.GONE);
                } else {
                    finish();
                }
            }
        });

        findViewById(R.id.sendEmail).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO); // Use ACTION_SENDTO
            intent.setData(Uri.parse("mailto:parthjamkhedkarjnec@gmail.com"));

            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(HelpSupportActivity.this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void toggleQuestion(View layout, TextView answer, ImageView arrow) {
        boolean isVisible = answer.getVisibility() == View.VISIBLE;

        // Collapse all other answers before expanding the selected one
        collapseAllQuestions();

        if (isVisible) {
            // Hide answer with smooth animation
            animateCollapse(answer);
            rotateArrow(arrow, 180f, 0f);
        } else {
            // Show answer with smooth animation
            animateExpand(answer);
            rotateArrow(arrow, 0f, 180f);
        }
    }

    // Rotates the arrow smoothly
    private void rotateArrow(ImageView arrow, float from, float to) {
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(arrow, "rotation", from, to);
        rotateAnimator.setDuration(300);
        rotateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimator.start();
    }

    // Expands the answer with smooth animation
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

        animator.setDuration(300);
        animator.start();
    }

    // Collapses the answer smoothly
    private void animateCollapse(TextView answer) {
        int initialHeight = answer.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            answer.getLayoutParams().height = (int) animation.getAnimatedValue();
            answer.requestLayout();
        });

        animator.setDuration(300);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                answer.setVisibility(View.GONE);
            }
        });

        animator.start();
    }

    // Hides all other questions before opening a new one
    private void collapseAllQuestions() {
        collapseIfOpen(findViewById(R.id.question1_answer), findViewById(R.id.q1_arrow));
        collapseIfOpen(findViewById(R.id.question2_answer), findViewById(R.id.q2_arrow));
        collapseIfOpen(findViewById(R.id.question3_answer), findViewById(R.id.q3_arrow));
        collapseIfOpen(findViewById(R.id.question4_answer), findViewById(R.id.q4_arrow));
        collapseIfOpen(findViewById(R.id.question5_answer), findViewById(R.id.q5_arrow));
    }

    private void collapseIfOpen(TextView answer, ImageView arrow) {
        if (answer.getVisibility() == View.VISIBLE) {
            animateCollapse(answer);
            rotateArrow(arrow, 180f, 0f);
        }
    }

}