package com.takwolf.android.lock9;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class Lock9View extends ViewGroup {

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

    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private List<Pair<NodeView, NodeView>> lineList;
    private NodeView currentNode;
    private StringBuilder pwdSb;
    private CallBack callBack;

    private void init(Context context) {
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(20);
        paint.setColor(Color.rgb(4, 115, 157));
        paint.setAntiAlias(true);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        bitmap = Bitmap.createBitmap(dm.widthPixels, dm.widthPixels, Bitmap.Config.ARGB_8888); //屏幕宽度的画笔，足够使用
        canvas = new Canvas();
        canvas.setBitmap(bitmap);

        for (int n = 0; n < 9; n++) {
            NodeView node = new NodeView(context, n + 1);
            addView(node);
        }
        lineList = new ArrayList<Pair<NodeView,NodeView>>();
        pwdSb = new StringBuilder();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);
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
            break;
        case MotionEvent.ACTION_UP:
            //回调结果
            if (callBack != null) {
                callBack.onFinish(pwdSb.toString());
                pwdSb.delete(0, pwdSb.length() - 1);
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
            break;
        default:
            break;
        }
        return true;
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
     * 获取Node，返回null表示在两个结点之间
     * @param x
     * @param y
     * @return 在结点之间，返回null
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
     * 回调监听器
     * @param callBack
     */
    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 结点View
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
     * 回调监听器
     */
    public interface CallBack {

        public void onFinish(String password);

    }

}
