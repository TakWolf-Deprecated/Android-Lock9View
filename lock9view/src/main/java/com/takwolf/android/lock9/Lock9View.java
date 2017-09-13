package com.takwolf.android.lock9;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

public class Lock9View extends ViewGroup {

    /**
     * 节点相关定义
     */
    private final List<NodeView> nodeList = new ArrayList<>(); // 已经连线的节点链表

    private float x; // 当前手指坐标x
    private float y; // 当前手指坐标y

    /**
     * 布局和节点样式
     */
    private Drawable nodeSrc;
    private Drawable nodeOnSrc;
    private float nodeSize; // 节点大小，如果不为0，则忽略内边距和间距属性
    private float nodeAreaExpand; // 对节点的触摸区域进行扩展
    private int nodeOnAnim; // 节点点亮时的动画
    private int lineColor;
    private float lineWidth;
    private float padding; // 内边距
    private float spacing; // 节点间隔距离

    /**
     * 自动连接中间节点
     */
    private boolean autoLink;

    /**
     * 震动管理器
     */
    private Vibrator vibrator;
    private boolean enableVibrate;
    private int vibrateTime;

    /**
     * 画线用的画笔
     */
    private Paint paint;

    /**
     * 结果回调监听器接口
     */
    private GestureCallback callback;

    public interface GestureCallback {

        void onNodeConnected(@NonNull int[] numbers);

        void onGestureFinished(@NonNull int[] numbers);

    }

    public void setGestureCallback(@Nullable GestureCallback callback) {
        this.callback = callback;
    }

    /**
     * 构造函数
     */

    public Lock9View(@NonNull Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public Lock9View(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public Lock9View(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Lock9View(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 初始化
     */
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        // 获取定义的属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Lock9View, defStyleAttr, defStyleRes);

        nodeSrc = a.getDrawable(R.styleable.Lock9View_lock9_nodeSrc);
        nodeOnSrc = a.getDrawable(R.styleable.Lock9View_lock9_nodeOnSrc);
        nodeSize = a.getDimension(R.styleable.Lock9View_lock9_nodeSize, 0);
        nodeAreaExpand = a.getDimension(R.styleable.Lock9View_lock9_nodeAreaExpand, 0);
        nodeOnAnim = a.getResourceId(R.styleable.Lock9View_lock9_nodeOnAnim, 0);
        lineColor = a.getColor(R.styleable.Lock9View_lock9_lineColor, Color.argb(0, 0, 0, 0));
        lineWidth = a.getDimension(R.styleable.Lock9View_lock9_lineWidth, 0);
        padding = a.getDimension(R.styleable.Lock9View_lock9_padding, 0);
        spacing = a.getDimension(R.styleable.Lock9View_lock9_spacing, 0);

        autoLink = a.getBoolean(R.styleable.Lock9View_lock9_autoLink, false);

        enableVibrate = a.getBoolean(R.styleable.Lock9View_lock9_enableVibrate, false);
        vibrateTime = a.getInt(R.styleable.Lock9View_lock9_vibrateTime, 20);

        a.recycle();

        // 初始化振动器
        if (enableVibrate && !isInEditMode()) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }

        // 初始化画笔
        paint = new Paint(Paint.DITHER_FLAG);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineColor);
        paint.setAntiAlias(true); // 抗锯齿

        // 构建node
        for (int n = 0; n < 9; n++) {
            NodeView node = new NodeView(getContext(), n + 1);
            addView(node);
        }

        // 清除FLAG，否则 onDraw() 不会调用，原因是 ViewGroup 默认透明背景不需要调用 onDraw()
        setWillNotDraw(false);
    }

    /**
     * 我们让高度等于宽度 - 方法有待验证
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = measureSize(widthMeasureSpec); // 测量宽度
        setMeasuredDimension(size, size);
    }

    /**
     * 测量长度
     */
    private int measureSize(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec); // 得到模式
        int specSize = MeasureSpec.getSize(measureSpec); // 得到尺寸
        switch (specMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                return specSize;
            case MeasureSpec.UNSPECIFIED:
            default:
                return 0;
        }
    }

    /**
     * 在这里进行node的布局
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            if (nodeSize > 0) { // 如果设置nodeSize值，则将节点绘制在九等分区域中心
                float areaWidth = (right - left) / 3;
                for (int n = 0; n < 9; n++) {
                    NodeView node = (NodeView) getChildAt(n);
                    // 获取3*3宫格内坐标
                    int row = n / 3;
                    int col = n % 3;
                    // 计算实际的坐标
                    int l = (int) (col * areaWidth + (areaWidth - nodeSize) / 2);
                    int t = (int) (row * areaWidth + (areaWidth - nodeSize) / 2);
                    int r = (int) (l + nodeSize);
                    int b = (int) (t + nodeSize);
                    node.layout(l, t, r, b);
                }
            } else { // 否则按照分割边距布局，手动计算节点大小
                float nodeSize = (right - left - padding * 2 - spacing * 2) / 3;
                for (int n = 0; n < 9; n++) {
                    NodeView node = (NodeView) getChildAt(n);
                    // 获取3*3宫格内坐标
                    int row = n / 3;
                    int col = n % 3;
                    // 计算实际的坐标，要包括内边距和分割边距
                    int l = (int) (padding + col * (nodeSize + spacing));
                    int t = (int) (padding + row * (nodeSize + spacing));
                    int r = (int) (l + nodeSize);
                    int b = (int) (t + nodeSize);
                    node.layout(l, t, r, b);
                }
            }
        }
    }

    /**
     * 在这里处理手势
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                x = event.getX(); // 这里要实时记录手指的坐标
                y = event.getY();
                NodeView currentNode = getNodeAt(x, y);
                if (currentNode != null && !currentNode.isHighLighted()) { // 碰触了新的未点亮节点
                    if (nodeList.size() > 0) { // 之前有点亮的节点
                        if (autoLink) { // 开启了中间节点自动连接
                            NodeView lastNode = nodeList.get(nodeList.size() - 1);
                            NodeView middleNode = getNodeBetween(lastNode, currentNode);
                            if (middleNode != null && !middleNode.isHighLighted()) { // 存在中间节点没点亮
                                // 点亮中间节点
                                middleNode.setHighLighted(true, true);
                                nodeList.add(middleNode);
                                handleOnNodeConnectedCallback();
                            }
                        }
                    }
                    // 点亮当前触摸节点
                    currentNode.setHighLighted(true, false);
                    nodeList.add(currentNode);
                    handleOnNodeConnectedCallback();
                }
                // 有点亮的节点才重绘
                if (nodeList.size() > 0) {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (nodeList.size() > 0) { // 有点亮的节点
                    // 手势完成
                    handleOnGestureFinishedCallback();
                    // 清除状态
                    nodeList.clear();
                    for (int n = 0; n < getChildCount(); n++) {
                        NodeView node = (NodeView) getChildAt(n);
                        node.setHighLighted(false, false);
                    }
                    // 通知重绘
                    invalidate();
                }
                break;
        }
        return true;
    }

    /**
     * 生成当前数字列表
     */
    @NonNull
    private int[] generateCurrentNumbers() {
        int[] numbers = new int[nodeList.size()];
        for (int i = 0; i < nodeList.size(); i++) {
            NodeView node = nodeList.get(i);
            numbers[i] = node.getNumber();
        }
        return numbers;
    }

    /**
     * 每次连接一个点
     */
    private void handleOnNodeConnectedCallback() {
        if (callback != null) {
            callback.onNodeConnected(generateCurrentNumbers());
        }
    }

    /**
     * 手势完成
     */
    private void handleOnGestureFinishedCallback() {
        if (callback != null) {
            callback.onGestureFinished(generateCurrentNumbers());
        }
    }

    /**
     * 系统绘制回调-主要绘制连线
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // 先绘制已有的连线
        for (int n = 1; n < nodeList.size(); n++) {
            NodeView firstNode = nodeList.get(n - 1);
            NodeView secondNode = nodeList.get(n);
            canvas.drawLine(firstNode.getCenterX(), firstNode.getCenterY(), secondNode.getCenterX(), secondNode.getCenterY(), paint);
        }
        // 如果已经有点亮的点，则在点亮点和手指位置之间绘制连线
        if (nodeList.size() > 0) {
            NodeView lastNode = nodeList.get(nodeList.size() - 1);
            canvas.drawLine(lastNode.getCenterX(), lastNode.getCenterY(), x, y, paint);
        }
    }

    /**
     * 获取给定坐标点的Node，返回null表示当前手指在两个Node之间
     */
    private NodeView getNodeAt(float x, float y) {
        for (int n = 0; n < getChildCount(); n++) {
            NodeView node = (NodeView) getChildAt(n);
            if (!(x >= node.getLeft() - nodeAreaExpand && x < node.getRight() + nodeAreaExpand)) {
                continue;
            }
            if (!(y >= node.getTop() - nodeAreaExpand && y < node.getBottom() + nodeAreaExpand)) {
                continue;
            }
            return node;
        }
        return null;
    }

    /**
     * 获取两个Node中间的Node，返回null表示没有中间node
     */
    @Nullable
    private NodeView getNodeBetween(@NonNull NodeView na, @NonNull NodeView nb) {
        if (na.getNumber() > nb.getNumber()) { // 保证 na 小于 nb
            NodeView nc = na;
            na = nb;
            nb = nc;
        }
        if (na.getNumber() % 3 == 1 && nb.getNumber() - na.getNumber() == 2) { // 水平的情况
            return (NodeView) getChildAt(na.getNumber());
        } else if (na.getNumber() <= 3 && nb.getNumber() - na.getNumber() == 6) { // 垂直的情况
            return (NodeView) getChildAt(na.getNumber() + 2);
        } else if ((na.getNumber() == 1 && nb.getNumber() == 9) || (na.getNumber() == 3 && nb.getNumber() == 7)) { // 倾斜的情况
            return (NodeView) getChildAt(4);
        } else {
            return null;
        }
    }

    /**
     * 节点描述类
     */
    private final class NodeView extends View {

        private int number;
        private boolean highLighted = false;

        NodeView(Context context, int number) {
            super(context);
            this.number = number;
            //noinspection deprecation
            setBackgroundDrawable(nodeSrc);
        }

        boolean isHighLighted() {
            return highLighted;
        }

        void setHighLighted(boolean highLighted, boolean isMid) {
            if (this.highLighted != highLighted) {
                this.highLighted = highLighted;
                if (nodeOnSrc != null) { // 没有设置高亮图片则不变化
                    //noinspection deprecation
                    setBackgroundDrawable(highLighted ? nodeOnSrc : nodeSrc);
                }
                if (nodeOnAnim != 0) { // 播放动画
                    if (highLighted) {
                        startAnimation(AnimationUtils.loadAnimation(getContext(), nodeOnAnim));
                    } else {
                        clearAnimation();
                    }
                }
                if (enableVibrate && !isMid) { // 震动
                    if (highLighted) {
                        vibrator.vibrate(vibrateTime);
                    }
                }
            }
        }

        int getCenterX() {
            return (getLeft() + getRight()) / 2;
        }

        int getCenterY() {
            return (getTop() + getBottom()) / 2;
        }

        int getNumber() {
            return number;
        }

    }

}
