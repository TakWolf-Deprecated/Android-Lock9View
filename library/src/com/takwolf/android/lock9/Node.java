package com.takwolf.android.lock9;

import android.widget.ImageView;

public class Node {

    //边界位置
    private int left;
    private int right;
    private int top;
    private int bottom;

    //这个点对应的ImageView控件
    private ImageView imageView; 

    //中心点
    private int centerX;
    private int centerY;

    //是否是高亮(划过)
    private boolean highLighted;

    //代表这个Point对象代表的数字，从1开始
    private int num;

    public Node(int left, int right, int top, int bottom, ImageView imageView, int num) {
        super();
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.imageView = imageView;
        this.num = num;

        centerX = (left + right) / 2;
        centerY = (top + bottom) / 2;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public boolean isHighLighted() {
        return highLighted;
    }

    public void setHighLighted(boolean highLighted) {
        this.highLighted = highLighted;
        if (highLighted) {
            imageView.setBackgroundResource(R.drawable.lock_9_view_node_highlighted);
        } else {
            imageView.setBackgroundResource(R.drawable.lock_9_view_node_normal);
        }
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void layout() {
        imageView.layout(left, top, right, bottom);
    }

}
