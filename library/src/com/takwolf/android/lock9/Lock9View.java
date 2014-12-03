/*
 * Copyright 2015-2016 TakWolf (takwolf.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * <p>Android-Lock9View</p>
 * <p>九宫格锁屏视图组件，提供一个九宫格手势屏锁视图功能。</p>
 * <p>手势处理和连线绘制都有组件内部完成，你只需要绑定这个组件并设置手势完成后的回调对象即可。</p>
 * <p>请使用 setCallBack(CallBack callBack) 函数设置回调对象。</p>
 * <p>需要注意的是，不论如何设置，组件的高度永远等于宽度。</p>
 * @author TakWolf (<a href="mailto:takwolf@foxmail.com">takwolf@foxmail.com</a>)
 */
public class Lock9View extends ViewGroup {

    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;

    private List<Pair<NodeView, NodeView>> lineList;
    private NodeView currentNode;

    private StringBuilder pwdSb;
    private CallBack callBack;

    public Lock9View(Context context) {
        super(context);
        init(context);
    }

    public Lock9View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(20);
        paint.setColor(Color.rgb(4, 115, 157)); //这里可以更改连线颜色
        paint.setAntiAlias(true);

        DisplayMetrics dm = context.getResources().getDisplayMetrics(); //bitmap的宽度是屏幕宽度，足够使用
        bitmap = Bitmap.createBitmap(dm.widthPixels, dm.widthPixels, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);

        for (int n = 0; n < 9; n++) {
            NodeView node = new NodeView(context, n + 1);
            addView(node);
        }
        lineList = new ArrayList<Pair<NodeView,NodeView>>();
        pwdSb = new StringBuilder();

        //清除FLAG，否则 onDraw() 不会调用，原因是 ViewGroup 默认透明背景不需要调用 onDraw()
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec); //我们让高度等于宽度
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!changed) {
            return;
        }
        int width = right - left;
        int nodeWidth = width / 3;
        int nodePadding = nodeWidth / 6;
        for (int n = 0; n < 9; n++) {
            NodeView node = (NodeView) getChildAt(n);
            int row = n / 3;
            int col = n % 3;
            int l = col * nodeWidth + nodePadding;
            int t = row * nodeWidth + nodePadding; 
            int r = col * nodeWidth + nodeWidth - nodePadding;
            int b = row * nodeWidth + nodeWidth - nodePadding;
            node.layout(l, t, r, b);
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
            if (nodeAt == null && currentNode == null) { //不需要画线，之前没接触点，当前也没接触点
                return true;
            } else { //需要画线
                clearScreenAndDrawList(); //清除所有图像，如果已有线，则重新绘制
                if (currentNode == null) { //第一个点 nodeAt不为null
                    currentNode = nodeAt;
                    currentNode.setHighLighted(true);
                    pwdSb.append(currentNode.getNum());
                } 
                else if (nodeAt == null || nodeAt.isHighLighted()) { //已经有点了，当前并未碰触新点
                    //以currentNode中心和当前触摸点开始画线
                    canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), event.getX(), event.getY(), paint);
                } else { //移动到新点
                    canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), nodeAt.getCenterX(), nodeAt.getCenterY(), paint);// 画线
                    nodeAt.setHighLighted(true);
                    Pair<NodeView, NodeView> pair = new Pair<NodeView, NodeView>(currentNode, nodeAt);
                    lineList.add(pair);
                    // 赋值当前的node
                    currentNode = nodeAt;
                    pwdSb.append(currentNode.getNum());
                }
                //通知onDraw重绘
                invalidate();
            }
            return true;
        case MotionEvent.ACTION_UP:
            //还没有触摸到点
            if (pwdSb.length() <= 0) {
                return super.onTouchEvent(event);
            }
            //回调结果
            if (callBack != null) {
                callBack.onFinish(pwdSb.toString());
                pwdSb.setLength(0); //清空
            }
            //清空保存点的集合
            currentNode = null;
            lineList.clear();
            clearScreenAndDrawList();
            //清除高亮
            for (int n = 0; n < getChildCount(); n++) {
                NodeView node = (NodeView) getChildAt(n);
                node.setHighLighted(false);
            }
            //通知onDraw重绘
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
     * 设置手势结果的回调监听器
     * @param callBack
     */
    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 节点描述类
     */
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

    /**
     * 结果回调监听器接口
     */
    public interface CallBack {

        public void onFinish(String password);

    }

}
