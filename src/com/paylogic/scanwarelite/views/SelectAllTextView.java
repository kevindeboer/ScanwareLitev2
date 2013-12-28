package com.paylogic.scanwarelite.views;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.R.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

public class SelectAllTextView extends TextView {
	private Paint linePaint;

	public SelectAllTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SelectAllTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SelectAllTextView(Context context) {
		super(context);
		init();
	}

	private void init() {
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(getResources().getColor(
				R.color.selectAllTextViewLine));
		linePaint.setStrokeWidth(3);

	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawLine(0, 0, 0, getMeasuredHeight(), linePaint);
		canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(),
				getMeasuredHeight(), linePaint);
		super.onDraw(canvas);
	}

}
