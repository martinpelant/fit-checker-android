package cz.mpelant.fitchecker.ui;

import android.os.Build;
import android.widget.EditText;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import cz.mpelant.fitchecker.R;

/**
 * Layout which an {@link EditText} to show a floating label when the hint is hidden
 * due to the user inputting text.
 *
 * @see <a href="https://dribbble.com/shots/1254439--GIF-Mobile-Form-Interaction">Matt D. Smith on Dribble</a>
 * @see <a href="http://bradfrostweb.com/blog/post/float-label-pattern/">Brad Frost's blog post</a>
 */
public final class FloatLabelLayout extends android.widget.FrameLayout {

    private static final long ANIMATION_DURATION = 150;

    private static final float DEFAULT_PADDING_LEFT_RIGHT_DP = 12f;

    private android.widget.EditText mEditText;
    private android.widget.TextView mLabel;

    public FloatLabelLayout(android.content.Context context) {
        this(context, null);
    }

    public FloatLabelLayout(android.content.Context context, android.util.AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatLabelLayout(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final android.content.res.TypedArray a = context
                .obtainStyledAttributes(attrs, R.styleable.FloatLabelLayout);

        final int sidePadding = a.getDimensionPixelSize(
                R.styleable.FloatLabelLayout_floatLabelSidePadding,
                dipsToPix(DEFAULT_PADDING_LEFT_RIGHT_DP));
        mLabel = new android.widget.TextView(context);
        mLabel.setPadding(sidePadding, 0, sidePadding, 0);
        mLabel.setVisibility(INVISIBLE);

        mLabel.setTextAppearance(context,
                a.getResourceId(R.styleable.FloatLabelLayout_floatLabelTextAppearance,
                        android.R.style.TextAppearance_Small)
        );

        addView(mLabel, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);

        a.recycle();
    }

    @Override
    public final void addView(android.view.View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (child instanceof android.widget.EditText) {
            // If we already have an EditText, throw an exception
            if (mEditText != null) {
                throw new IllegalArgumentException("We already have an EditText, can only have one");
            }

            // Update the layout params so that the EditText is at the bottom, with enough top
            // margin to show the label
            final android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(params);
            lp.gravity = android.view.Gravity.BOTTOM;
            lp.topMargin = (int) mLabel.getTextSize();
            params = lp;

            setEditText((android.widget.EditText) child);
        }

        // Carry on adding the View...
        super.addView(child, index, params);
    }

    private void setEditText(android.widget.EditText editText) {
        mEditText = editText;

        // Add a TextWatcher so that we know when the text input has changed
        mEditText.addTextChangedListener(new android.text.TextWatcher() {

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (android.text.TextUtils.isEmpty(s)) {
                    // The text is empty, so hide the label if it is visible
                    if (mLabel.getVisibility() == android.view.View.VISIBLE) {
                        hideLabel();
                    }
                } else {
                    // The text is not empty, so show the label if it is not visible
                    if (mLabel.getVisibility() != android.view.View.VISIBLE) {
                        showLabel();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        // Add focus listener to the EditText so that we can notify the label that it is activated.
        // Allows the use of a ColorStateList for the text color on the label
        mEditText.setOnFocusChangeListener(new android.view.View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(android.view.View view, boolean focused) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    mLabel.setActivated(focused);
                }
            }
        });

        mLabel.setText(mEditText.getHint());
    }

    /**
     * @return the {@link EditText} text input
     */
    public android.widget.EditText getEditText() {
        return mEditText;
    }

    /**
     * @return the {@link TextView} label
     */
    public android.widget.TextView getLabel() {
        return mLabel;
    }

    /**
     * Show the label using an animation
     */
    private void showLabel() {
        mLabel.setVisibility(android.view.View.VISIBLE);
        ViewHelper.setAlpha(mLabel, 0f);
        ViewHelper.setTranslationY(mLabel, mLabel.getHeight());
        ViewPropertyAnimator.animate(mLabel)
                .alpha(1f)
                .translationY(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(null).start();
    }

    /**
     * Hide the label using an animation
     */
    private void hideLabel() {
        ViewHelper.setAlpha(mLabel, 1f);
        ViewHelper.setTranslationY(mLabel, 0f);
        ViewPropertyAnimator.animate(mLabel)
                .alpha(0f)
                .translationY(mLabel.getHeight())
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mLabel.setVisibility(android.view.View.GONE);
                    }
                }).start();
    }

    /**
     * Helper method to convert dips to pixels.
     */
    private int dipsToPix(float dps) {
        return (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, dps,
                getResources().getDisplayMetrics());
    }
}