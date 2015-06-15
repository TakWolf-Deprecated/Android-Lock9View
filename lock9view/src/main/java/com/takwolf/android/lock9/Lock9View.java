/*
 * Copyright 2015-2016 TakWolf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.takwolf.android.lock9;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class Lock9View extends ViewGroup {

    // TODO 其实可以不需要独立的画布
    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;

    private List<Pair<NodeView, NodeView>> lineList;
    private NodeView currentNode;

    /**
     * 密码构建器
     */
    private StringBuilder passwordBuilder = new StringBuilder();

    /**
     * 自定义属性列表
     */
    private Drawable nodeSrc;
    private Drawable nodeOnSrc;
    private int lineColor;
    private float lineWidth;
    private float padding;
    private float spacing;

    /**
     * 结果回调监听器接口
     */
    private CallBack callBack;

    public interface CallBack {

        public void onFinish(String password);

    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 构造函数
     */
    public Lock9View(Context context) {
        this(context, null);
    }

    public Lock9View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr); // TODO api 21
        initFromAttributes(attrs, defStyleAttr);
    }

    /**
     * 初始化
     */
    private void initFromAttributes(AttributeSet attrs, int defStyleAttr) {
        // 初始化属性
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Lock9View, defStyleAttr, 0);

        nodeSrc = a.getDrawable(R.styleable.Lock9View_lock9_nodeSrc);
        nodeOnSrc = a.getDrawable(R.styleable.Lock9View_lock9_nodeOnSrc);
        lineColor = a.getColor(R.styleable.Lock9View_lock9_lineColor, Color.argb(0, 0, 0, 0));
        lineWidth = a.getDimension(R.styleable.Lock9View_lock9_lineWidth, 0);
        padding = a.getDimension(R.styleable.Lock9View_lock9_padding, 0);
        spacing = a.getDimension(R.styleable.Lock9View_lock9_spacing, 0);

        a.recycle();

        // TODO 初始化画布-这里可以改进
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineColor);
        paint.setAntiAlias(true);

        DisplayMetrics dm = getResources().getDisplayMetrics(); // bitmap的宽度是屏幕宽度，足够使用
        bitmap = Bitmap.createBitmap(dm.widthPixels, dm.widthPixels, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);

        // 构建node
        for (int n = 0; n < 9; n++) {
            NodeView node = new NodeView(getContext(), n + 1);
            addView(node);
        }
        lineList = new ArrayList<Pair<NodeView,NodeView>>();

        // 清除FLAG，否则 onDraw() 不会调用，原因是 ViewGroup 默认透明背景不需要调用 onDraw()
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec); // 我们让高度等于宽度
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            int nodeWidth = (int) ((right - left - padding * 2 - spacing * 2) / 3);
            for (int n = 0; n < 9; n++) {
                NodeView node = (NodeView) getChildAt(n);
                // 获取3*3宫格内坐标
                int row = n / 3;
                int col = n % 3;
                // 计算实际的坐标，要包括内边距和分割边距
                int l = (int) (padding + col * (nodeWidth + spacing));
                int t = (int) (padding + row * (nodeWidth + spacing));
                int r = l + nodeWidth;
                int b = t + nodeWidth;
                node.layout(l, t, r, b);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                NodeView nodeAt = getNodeAt(event.getX(), event.getY());
                if (nodeAt == null && currentNode == null) { // 不需要画线，之前没接触点，当前也没接触点
                    return true;
                } else { // 需要画线
                    clearScreenAndDrawList(); // 清除所有图像，如果已有线，则重新绘制
                    if (currentNode == null) { // 第一个点 nodeAt不为null
                        currentNode = nodeAt;
                        currentNode.setHighLighted(true);
                        passwordBuilder.append(currentNode.getNum());
                    }
                    else if (nodeAt == null || nodeAt.isHighLighted()) { // 已经有点了，当前并未碰触新点
                        // 以currentNode中心和当前触摸点开始画线
                        canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), event.getX(), event.getY(), paint);
                    } else { // 移动到新点
                        canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), nodeAt.getCenterX(), nodeAt.getCenterY(), paint);// 画线
                        nodeAt.setHighLighted(true);
                        Pair<NodeView, NodeView> pair = new Pair<NodeView, NodeView>(currentNode, nodeAt);
                        lineList.add(pair);
                        // 赋值当前的node
                        currentNode = nodeAt;
                        passwordBuilder.append(currentNode.getNum());
                    }
                    // 通知onDraw重绘
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                // 还没有触摸到点
                if (passwordBuilder.length() <= 0) {
                    return super.onTouchEvent(event);
                }
                // 回调结果
                if (callBack != null) {
                    callBack.onFinish(passwordBuilder.toString());
                    passwordBuilder.setLength(0); // 清空
                }
                // 清空保存点的集合
                currentNode = null;
                lineList.clear();
                clearScreenAndDrawList();
                // 清除高亮
                for (int n = 0; n < getChildCount(); n++) {
                    NodeView node = (NodeView) getChildAt(n);
                    node.setHighLighted(false);
                }
                // 通知onDraw重绘
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 清掉屏幕上所有的线，然后画出集合里面的线
     */
    private void clearScreenAndDrawList() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        for (Pair<NodeView, NodeView> pair : lineList) {
            canvas.drawLine(pair.first.getCenterX(), pair.first.getCenterY(), pair.second.getCenterX(), pair.second.getCenterY(), paint);
        }
    }

    /**
     * 获取Node，返回null表示当前手指在两个Node之间
     */
    private NodeView getNodeAt(float x, float y) {
        for (int n = 0; n < getChildCount(); n++) {
            NodeView node = (NodeView) getChildAt(n);
            if (!(x >= node.getLeft() && x < node.getRight())) {
                continue;
            }
            if (!(y >= node.getTop() && y < node.getBottom())) {
                continue;
            }
            return node;
        }
        return null;
    }

    /**
     * 结点描述类
     */
    private class NodeView extends View {

        private int num;
        private boolean highLighted = false;

        public NodeView(Context context, int num) {
            super(context);
            this.num = num;
            setBackgroundDrawable(nodeSrc);
        }

        public boolean isHighLighted() {
            return highLighted;
        }

        public void setHighLighted(boolean highLighted) {
            if (this.highLighted != highLighted) {
                this.highLighted = highLighted;
                setBackgroundDrawable(highLighted ? nodeOnSrc : nodeSrc);
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

    }

}
