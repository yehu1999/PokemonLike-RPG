package theEntities;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * 宝可梦对象
 * yehu1999
 */
public class Pokemon extends Sprite{
	
	//宝可梦常量数据
	public static final float SPEED = 60 * 1.0f;             //初始速度
	public static final float gravity = 60 * 1.8f;           //重力
	public static final float friction = 60 * 0.5f;          //摩擦力
	public static final String blockedKey = "blocked";       //碰撞属性关键字
	public static final int MAXLIVES = 100;                  //最大生命值

	//宝可梦变量数据
	private Vector2 velocity = new Vector2(); //速度向量
	private float speed = SPEED;              //速度大小
	private boolean isMove_UP = false, isMove_DOWN = false, isMove_LEFT = false, isMove_RIGHT = false;//移动状态标志
	
	public TiledMapTileLayer backgroundLayer; //Map背景图层
	private float increment;                   //步长
	
	private Animation<TextureRegion> animationUp ,animationDown , animationLeft, animationRight;      //动画
	float animationTime = 0;                  //动画时间

	public int NO;                            //编号
	public Boolean isLive = false;            //是否存活
	public int lives = 100;                   //生命值
	public boolean isAngry = false;           //是否被激怒
	public boolean isAttacking = false;       //是否正在攻击
	private int NearAttackValue = 45;         //近战伤害
	private int farAttackValue = 30;          //远战伤害
	
	private Vector2 bornPosition[] = new Vector2[20];//生成地点

	public Pokemon() {
	
	}
	
	public Pokemon(Animation<TextureRegion> up, Animation<TextureRegion> down, Animation<TextureRegion> left, Animation<TextureRegion> right, TiledMapTileLayer backgroundLayer) {
		super(down.getKeyFrame(0));
		this.animationUp = up;
		this.animationDown = down;
		this.animationLeft = left;
		this.animationRight = right;
		this.backgroundLayer = backgroundLayer;
	}
	
	@Override
	public void draw(Batch batch) {
		update(Gdx.graphics.getDeltaTime());
		super.draw(batch);
	}
	
	public void update(float delta) {	
		
		//应用重力
		//velocity.y -= gravity * delta;
		
		//应用摩擦力
		if(!isAngry) {
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
		increment = backgroundLayer.getTileWidth();
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
		increment = backgroundLayer.getTileHeight();
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
		}else {//Still动画资源缺失
			setRegion((TextureRegion) animationDown.getKeyFrame(animationTime));
		}
		
	}
	
	//砖块是否实心
	private boolean isCellBlocked(float x, float y) {
		Cell cell = backgroundLayer.getCell((int) (x / backgroundLayer.getTileWidth()), (int) (y / backgroundLayer.getTileHeight()));
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
	
	//砖块是否可生成宝可梦
	private boolean isCellGenerable(float x, float y) {
		Cell cell = backgroundLayer.getCell((int) (x / backgroundLayer.getTileWidth()), (int) (y / backgroundLayer.getTileHeight()));
		return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
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

	public TiledMapTileLayer getBackgroundLayer() {
		return backgroundLayer;
	}

	public void setBackgroundLayer(TiledMapTileLayer backgroundLayer) {
		this.backgroundLayer = backgroundLayer;
	}
	
	//简单怪物AI
	public void SimpleAI(float playerX, float playerY) {
		
		float disX = playerX - getX();
		float disY = playerY - getY();
		
		boolean isTooCloseX = (disX >= -16 && disX <= 16);
		boolean isCanSeeYouX = (disX < 200  && disX > 16) || (disX > -200 && disX < -16);
		
		boolean isTooCloseY = (disY >= -16 && disY <= 16);
		boolean isCanSeeYouY = (disY < 200  && disY > 16) || (disY > -200 && disY < -16);
		
		if(isLive) {//如果活着
			
			//如果不是满血还靠太近
			if(lives < MAXLIVES && isTooCloseX && isTooCloseY)
				isAngry = true;
			//如果残血并且能看到你
			if(lives < MAXLIVES / 2 && isCanSeeYouX && isCanSeeYouY)
				isAngry = true;
			
			if(isAngry) {//如果被激怒
				//简陋的自动寻路
				if(isCanSeeYouX) {
					
					if(disX > 0) {
			        	velocity.x = speed;
						isMove_RIGHT = true;
						animationTime = 0;
			        }else{
			        	velocity.x = -speed;
						isMove_LEFT = true;
						animationTime = 0;
			        }
				}else if(isTooCloseX) {
					;
				}else {
					isAngry = false;
					isAttacking = false;
					isMove_UP = false;
		        	isMove_DOWN = false;
		        	isMove_RIGHT = false;
		        	isMove_LEFT = false;
		        	animationTime = 0;
					
				}
				if(isCanSeeYouY) {
					if(disY > 0) {
			        	velocity.y = speed;
						isMove_UP = true;
						animationTime = 0;
			        }else{
			        	velocity.y = -speed;
						isMove_DOWN = true;
						animationTime = 0;
			        }
				}else if(isTooCloseY) {
					;
				}else {
					isAngry = false;
					isAttacking = false;
					isMove_UP = false;
		        	isMove_DOWN = false;
		        	isMove_RIGHT = false;
		        	isMove_LEFT = false;
		        	animationTime = 0;
				}
				
				//攻击
				isAttacking = true;
				if(isCanSeeYouX && isCanSeeYouY) {
					//远程攻击
					/*
					 * Wait For Build
					 */
				}
				
			}else {//如果平静
				isAttacking = false;
				
				// 创建一个 Random 对象
				Random random = new Random();
				// 生成随机整数
		        int randomNumber = random.nextInt();
		        int minRange = 1;
		        int maxRange = 5000;
		        int randomInRange = random.nextInt(maxRange - minRange + 1) + minRange;
			
		        //随机移动
		        if(randomInRange <= 25) {
		        	velocity.y = speed;
					isMove_UP = true;
					animationTime = 0;
		        }else if(randomInRange > 25 && randomInRange < 50) {
		        	velocity.y = -speed;
					isMove_DOWN = true;
					animationTime = 0;
		        }else if(randomInRange > 50 && randomInRange <= 75) {
		        	velocity.x = -speed;
					isMove_LEFT = true;
					animationTime = 0;
		        }else if(randomInRange >75 && randomInRange <= 100) {
		        	velocity.x = speed;
					isMove_RIGHT = true;
					animationTime = 0;
		        }else if(randomInRange >100 && randomInRange <= 500) {
		        	;
		        }else{
		        	isMove_UP = false;
		        	isMove_DOWN = false;
		        	isMove_RIGHT = false;
		        	isMove_LEFT = false;
		        	animationTime = 0;
		        }
			}
		}
	}

	public boolean generate(int num, float meetodd) {
		if(!isLive) {
    		// 创建一个 Random 对象
			Random random = new Random();
			// 生成随机整数
	        int randomNumber = random.nextInt();
	        int minRange = 1;
	        int maxRange = 100;
	        int randomInRange = random.nextInt(maxRange - minRange + 1) + minRange;
	        
	        bornPosition[0] = new Vector2(150,100);
	        bornPosition[1] = new Vector2(125,125);
	        bornPosition[2] = new Vector2(250,250);
	        bornPosition[3] = new Vector2(250,350);
	        bornPosition[4] = new Vector2(100,350);
	        bornPosition[5] = new Vector2(60,420);
	        bornPosition[6] = new Vector2(400,350);
	        bornPosition[7] = new Vector2(400,300);
	        bornPosition[8] = new Vector2(400,400);
	        bornPosition[9] = new Vector2(350,150);
	        bornPosition[10] = new Vector2(400,150);
	        
	        if(randomInRange <= 100) {
	        	lives = MAXLIVES;
	        	isLive = true;
	        	NO = num;
	        	setPosition(bornPosition[num].x * backgroundLayer.getTileWidth(), (backgroundLayer.getHeight() - bornPosition[num].y) * backgroundLayer.getTileHeight());
	        	
	        	return true;
	        }
	        
    	}
		return false;
	}
	
	
}
