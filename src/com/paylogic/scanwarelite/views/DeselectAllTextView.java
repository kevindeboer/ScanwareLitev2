package com.paylogic.scanwarelite.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;

public class DeselectAllTextView extends TextView {
	private Paint linePaint;
	
	public DeselectAllTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DeselectAllTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DeselectAllTextView(Context context) {
		super(context);
		init();
	}

	private void init() {
		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		linePaint.setColor(getResources().getColor(R.color.deselectAllTextViewLine));
		linePaint.setStrokeWidth(3);

	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.drawLine(0, 0, 0, getMeasuredHeight(), linePaint);
		canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), linePaint);
		super.onDraw(canvas);
	}


}
