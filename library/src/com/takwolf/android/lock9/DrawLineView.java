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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

public class DrawLineView extends View {

    private float movX;// 声明起点坐标
    private float movY;
    private Paint paint; //声明画笔
    private Canvas canvas; //画布
    private Bitmap bitmap; //位图

    private List<Node> nodeList;
    private List<Pair<Node, Node>> lineList;

    //手指当前在哪个Point内
    private Node currentNode; 

    //完成回调
    private CallBack callBack;

    // 用户当前绘制的图形密码
    private StringBuilder passWordSb;

    private DrawLineView(Context context) {
    	super(context);
    }

    public DrawLineView(Context context, List<Node> nodeList) {
        this(context);
        paint = new Paint(Paint.DITHER_FLAG);// 创建一个画笔
        bitmap = Bitmap.createBitmap(480, 854, Bitmap.Config.ARGB_8888); // 设置位图的宽高
        canvas = new Canvas();
        canvas.setBitmap(bitmap);

        paint.setStyle(Style.STROKE); // 设置非填充
        paint.setStrokeWidth(10); // 笔宽5像素
        paint.setColor(Color.rgb(4, 115, 157)); //设置颜色
        paint.setAntiAlias(true);// 不显示锯齿

        this.nodeList = nodeList;
        lineList = new ArrayList<Pair<Node, Node>>();

        passWordSb = new StringBuilder();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN :
            movX = event.getX();
            movY = event.getY();
            currentNode = getNodeAt(movX, movY);
            if (currentNode != null) {
                currentNode.setHighLighted(true);
                passWordSb.append(currentNode.getNum());
            }
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE :
            clearScreenAndDrawList();
            Node nodeAt = getNodeAt(event.getX(), event.getY());
            //代表当前用户手指处于点与点之前
            if (currentNode == null && nodeAt == null) {
                return true;
            } else { //代表用户的手指移动到了点上
                if (currentNode == null) {//先判断当前的point是不是为null
                    //如果为空，那么把手指移动到的点赋值给currentPoint
                    currentNode = nodeAt;
                    //把currentPoint这个点设置选中为true;
                    currentNode.setHighLighted(true);
                    passWordSb.append(currentNode.getNum());
                }
            }
            if (nodeAt == null || currentNode.equals(nodeAt) || nodeAt.isHighLighted()) {
                // 点击移动区域不在圆的区域 或者
                // 如果当前点击的点与当前移动到的点的位置相同
                // 那么以当前的点中心为起点，以手指移动位置为终点画线
                canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), event.getX(), event.getY(), paint); //画线
            } else {
                // 如果当前点击的点与当前移动到的点的位置不同
                // 那么以前前点的中心为起点，以手移动到的点的位置画线
                canvas.drawLine(currentNode.getCenterX(), currentNode.getCenterY(), nodeAt.getCenterX(), nodeAt.getCenterY(), paint);// 画线
                nodeAt.setHighLighted(true);
                Pair<Node, Node> pair = new Pair<Node, Node>(currentNode, nodeAt);
                lineList.add(pair);
                // 赋值当前的node
                currentNode = nodeAt;
                passWordSb.append(currentNode.getNum());
            }
            invalidate();
            break;
        case MotionEvent.ACTION_UP :
            //当手指抬起的时候
            //清掉屏幕上所有的线，只画上集合里面保存的线
            // TODO 解锁完成之后进行回调
            if (callBack != null) {
                callBack.onFinish(passWordSb.toString());
            }
            //重置passWordSb
            passWordSb = new StringBuilder();
            //清空保存点的集合
            lineList.clear();
            //重新绘制界面
            clearScreenAndDrawList();
            for (Node p : nodeList) {
                p.setHighLighted(false);
            }
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
        for (Pair<Node, Node> pair : lineList) {
            canvas.drawLine(pair.first.getCenterX(), pair.first.getCenterY(), pair.second.getCenterX(), pair.second.getCenterY(), paint);// 画线
        }
    }

    /**
     * 通过坐标获取Point，返回null代表用户当前移动的地方属于点与点之间
     * @param x
     * @param y
     * @return 如果没有找到，则返回null，
     */
    private Node getNodeAt(float x, float y) {
        for (Node point : nodeList) {
            if (!(x >= point.getLeft() && x < point.getRight())) { //判断x坐标
                continue;
            }
            if (!(y >= point.getTop() && y < point.getBottom())) { //判断y坐标
                continue;
            }
            return point;
        }
        return null;
    }

    /**
     * 输入完成后回调接口
     */
    public interface CallBack {

        public void onFinish(String password);

    }

}
