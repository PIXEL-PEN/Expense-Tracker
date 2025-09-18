package com.example.expensetracker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint = new Paint();

    public DividerItemDecoration() {
        paint.setColor(0xFFCCCCCC); // light gray
        paint.setStrokeWidth(2f);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) { // skip last item
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int bottom = child.getBottom() + params.bottomMargin;
            c.drawLine(left, bottom, right, bottom, paint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.bottom = 2; // spacing for divider line
    }
}
