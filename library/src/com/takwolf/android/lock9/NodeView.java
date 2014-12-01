package com.takwolf.android.lock9;

import android.content.Context;
import android.view.View;

public class NodeView extends View {

	private int num;
	private boolean highLighted;

	private NodeView(Context context) {
		super(context);
	}

	public NodeView(Context context, int num) {
		this(context);
		this.num = num;
		highLighted = false;
        setBackgroundResource(R.drawable.lock_9_view_node_normal);
	}

    public boolean isHighLighted() {
        return highLighted;
    }

    public void setHighLighted(boolean highLighted) {
        this.highLighted = highLighted;
        if (highLighted) {
            setBackgroundResource(R.drawable.lock_9_view_node_highlighted);
        } else {
            setBackgroundResource(R.drawable.lock_9_view_node_normal);
        }
    }

    public int getCenterX() {
        return (getLeft() + getRight()) / 2;
    }

    public int getCenterY() {
        return (getTop() + getBottom()) / 2;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

}
