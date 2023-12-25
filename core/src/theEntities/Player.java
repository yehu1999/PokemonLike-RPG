package theEntities;

import java.lang.constant.Constable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

/**
 * 玩家对象
 * yehu1999
 */
public class Player extends Sprite implements InputProcessor{//implements InputProcessor引入控制输入接口
	
	//玩家常量数据
	public static final float POISTION_X = 250 , POISTION_Y = 350;//初始位置
	public static final float SPEED = 60 * 2;                //初始速度
	public static final float gravity = 60 * 1.8f;           //重力
	public static final float friction = 60 * 2.0f;          //摩擦力
	public static final float NORMAL_VIEW = 1.0f;            //正常的视野缩放
	public static final float STRANGE_VIEW = 7.5f;           //奇怪的视野缩放
	
	public static final int MAXLIVES = 100;                  //最大生命值

	//玩家变量数据
	private Boolean isLive = true;             //是否存活
	private int lives = MAXLIVES;              //生命值
	
	private Vector2 velocity = new Vector2();  //速度向量
	private float speed = SPEED;               //速度大小
	private boolean isMove_UP = false, isMove_DOWN = false, isMove_LEFT = false, isMove_RIGHT = false;//移动状态标志
	
	public float view = NORMAL_VIEW;                         //当前视野缩放
	
	private TiledMapTileLayer collisionLayer;  //碰撞层
	private String blockedKey = "blocked";     //碰撞属性关键字
	private float increment;                   //步长
	
	private Animation<TextureRegion> animationStill, animationUp ,animationDown , animationLeft, animationRight;      //动画
	float animationTime = 0;                   //动画时间
	
	public Player(Animation<TextureRegion> still, Animation<TextureRegion> up, Animation<TextureRegion> down, Animation<TextureRegion> left, Animation<TextureRegion> right, TiledMapTileLayer collisionLayer) {
		super(still.getKeyFrame(0));
		this.animationStill = still;
		this.animationUp = up;
		this.animationDown = down;
		this.animationLeft = left;
		this.animationRight = right;
		this.collisionLayer = collisionLayer;
		//setScale(1);
		//setSize(collisionLayer.getWidth() / 3, collisionLayer.getHeight() * 1.25f);
	}
	
	
	@Override
	public void draw(Batch batch) {
		update(Gdx.graphics.getDeltaTime());
		super.draw(batch);
	}
	
	public void update(float delta) {
		//视野变换
		float oldView = view;
		if(getY() < 150 * getCollisionLayer().getTileHeight())
			view = STRANGE_VIEW;
		else
			view = NORMAL_VIEW;
			
		//应用重力
		//velocity.y -= gravity * delta;
		
		//应用摩擦力
		if(!isMove_RIGHT) { 
			if(velocity.x > friction)
				velocity.x -= friction * delta;
			if(velocity.x > 0 && velocity.x <= friction)
				velocity.x = 0;
		}
		if(!isMove_LEFT) { 
			if(velocity.x < -friction)
				velocity.x += friction * delta;
			if(velocity.x < 0 && velocity.x >= -friction)
				velocity.x = 0;
		}
		if(!isMove_UP) { 
			if(velocity.y > friction)
				velocity.y -= friction * delta;
			if(velocity.y > 0 && velocity.y <= friction)
				velocity.y = 0;
		}
		if(!isMove_DOWN) { 
			if(velocity.y < -friction)
				velocity.y += friction * delta;
			if(velocity.y < 0 && velocity.y >= -friction)
				velocity.y = 0;
		}
		
		//速度限制
		if(velocity.y > speed)
			velocity.y = speed;
		else if(velocity.y < -speed)
			velocity.y = -speed;
		
		//移动前位置
		float oldX = getX();
		float oldY = getY();
		
		//是否碰撞
		boolean collisionX = false;
		boolean collisionY = false;
		
		//水平移动
		setX(getX() + velocity.x * delta);
		//碰撞检测
		increment = collisionLayer.getTileWidth();
		increment = getWidth() < increment ? getWidth() / 2 : increment / 2;
		if(velocity.x < 0) //向左
			collisionX = collidesLeft();
		else if(velocity.x > 0) //向右
			collisionX = collidesRight();
		//碰撞反馈
		if(collisionX) {
			setX(oldX);
			//velocity.x = 0;
		}
		
		//竖直移动
		setY(getY() + velocity.y * delta);
		//碰撞检测
		increment = collisionLayer.getTileHeight();
		increment = getHeight() < increment ? getHeight() / 2 : increment / 2;
		if(velocity.y < 0) //向下移动
			collisionY = collidesBottom();
		else if(velocity.y > 0) //向上移动
			collisionY = collidesTop();
		//碰撞反馈
		if(collisionY) {
			setY(oldY);
			//velocity.y = 0;
		}
		
		//更新动画参数
		animationTime += delta;
		if(velocity.x < 0) {
			setRegion((TextureRegion) animationLeft.getKeyFrame(animationTime));
		}else if(velocity.x > 0) {
			setRegion((TextureRegion) animationRight.getKeyFrame(animationTime));
		}else if(velocity.y < 0) {
			setRegion((TextureRegion) animationDown.getKeyFrame(animationTime));
		}else if(velocity.y > 0) {
			setRegion((TextureRegion) animationUp.getKeyFrame(animationTime));
		}else {
			setRegion((TextureRegion) animationStill.getKeyFrame(animationTime));
		}
		
	}
	
	
	private boolean isCellBlocked(float x, float y) {
		Cell cell = collisionLayer.getCell((int) (x / collisionLayer.getTileWidth()), (int) (y / collisionLayer.getTileHeight()));
		return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
	}
	
	public boolean collidesRight() {
		for(float step = 0; step <= getHeight(); step += increment)
			if(isCellBlocked(getX() + getWidth(), getY() + step))
				return true;
		return false;
	}

	public boolean collidesLeft() {
		for(float step = 0; step <= getHeight(); step += increment)
			if(isCellBlocked(getX(), getY() + step))
				return true;
		return false;
	}

	public boolean collidesTop() {
		for(float step = 0; step <= getWidth(); step += increment)
			if(isCellBlocked(getX() + step, getY() + getHeight()))
				return true;
		return false;

	}

	public boolean collidesBottom() {
		for(float step = 0; step <= getWidth(); step += increment)
			if(isCellBlocked(getX() + step, getY()))
				return true;
		return false;
	}
	
	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public float getGravity() {
		return gravity;
	}

	public TiledMapTileLayer getCollisionLayer() {
		return collisionLayer;
	}

	public void setCollisionLayer(TiledMapTileLayer collisionLayer) {
		this.collisionLayer = collisionLayer;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		switch(keycode) {
		case Keys.W:
			velocity.y = speed;
			isMove_UP = true;
			animationTime = 0;
			break;
		case Keys.S:
			velocity.y = -speed;
			isMove_DOWN = true;
			animationTime = 0;
			break;
		case Keys.A:
			velocity.x = -speed;
			isMove_LEFT = true;
			animationTime = 0;
			break;
		case Keys.D:
			velocity.x = speed;
			isMove_RIGHT = true;
			animationTime = 0;

		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch(keycode) {
		case Keys.W:
			isMove_UP = false;
			animationTime = 0;
			break;
		case Keys.S:
			isMove_DOWN = false;
			animationTime = 0;
			break;
		case Keys.A:
			isMove_LEFT = false;
			animationTime = 0;
			break;
		case Keys.D:
			isMove_RIGHT = false;
			animationTime = 0;
			break;
			
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		// TODO Auto-generated method stub
		return false;
	}
}
