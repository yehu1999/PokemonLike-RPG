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
	public boolean isScary = false;           //是否害怕
	public boolean isAttacking = false;       //是否正在攻击
	private int NearAttackValue = 45;         //近战伤害
	private int farAttackValue = 30;          //远战伤害
	
	public boolean isFound = false;           //是否被发现
	
	private Vector2 bornPosition[] = new Vector2[99];//生成地点

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
		boolean isCanSeeYouX = (disX < 150  && disX > 16) || (disX > -150 && disX < -16);
		
		boolean isTooCloseY = (disY >= -16 && disY <= 16);
		boolean isCanSeeYouY = (disY < 200  && disY > 16) || (disY > -200 && disY < -16);
		
		if(isLive) {//如果活着
			
			//如果靠近
			if(isTooCloseX && isTooCloseY)
				isFound = true;
			//如果不是满血还靠太近
			if(lives < MAXLIVES && isTooCloseX && isTooCloseY)
				isAngry = true;
			//如果残血并且能看到你
			if(lives < MAXLIVES / 2 && isCanSeeYouX && isCanSeeYouY)
				isAngry = true;
			
			if(isAngry && !isScary) {//如果被激怒
				//简陋的自动寻路
				if(isCanSeeYouX && isCanSeeYouY) {
					
					if(disX > 0) {
			        	velocity.x = speed;
						isMove_RIGHT = true;
						animationTime = 0;
			        }else{
			        	velocity.x = -speed;
						isMove_LEFT = true;
						animationTime = 0;
			        }
					
					if(disY > 0) {
			        	velocity.y = speed;
						isMove_UP = true;
						animationTime = 0;
			        }else{
			        	velocity.y = -speed;
						isMove_DOWN = true;
						animationTime = 0;
			        }
				}else if(isTooCloseX && isTooCloseY) {
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
		        int maxRange = 10000;
		        int randomInRange = random.nextInt(maxRange - minRange + 1) + minRange;
			
		        if (isScary) {//逃跑
		        	//简陋的自动寻路
					if(isCanSeeYouX || isTooCloseX) {
						
						if(disX > 0) {
				        	velocity.x = -speed;
							isMove_LEFT = true;
							animationTime = 0;
				        }else{
				        	velocity.x = speed;
							isMove_RIGHT = true;
							animationTime = 0;
				        }
					}else {
						isAngry = false;
						isAttacking = false;
						isMove_UP = false;
			        	isMove_DOWN = false;
			        	isMove_RIGHT = false;
			        	isMove_LEFT = false;
			        	animationTime = 0;
						
					}
					if(isCanSeeYouY || isTooCloseY) {
						if(disY > 0) {
				        	velocity.y = -speed;
							isMove_DOWN = true;
							animationTime = 0;
				        }else{
				        	velocity.y = speed;
							isMove_UP = true;
							animationTime = 0;
				        }
					}else {
						isAngry = false;
						isAttacking = false;
						isMove_UP = false;
			        	isMove_DOWN = false;
			        	isMove_RIGHT = false;
			        	isMove_LEFT = false;
			        	animationTime = 0;
					}
		        }else {//随机移动
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
	        
	        //设置位置
	        bornPosition[0] = new Vector2(600,7000);
	        bornPosition[1] = new Vector2(600,6200);
	        bornPosition[2] = new Vector2(600,5300);
	        bornPosition[3] = new Vector2(600,4500);
	        bornPosition[4] = new Vector2(530,4000);
	        bornPosition[5] = new Vector2(1142,5063);
	        bornPosition[6] = new Vector2(1266,6585);
	        bornPosition[7] = new Vector2(2000,4400);
	        bornPosition[8] = new Vector2(2000,4844);
	        bornPosition[9] = new Vector2(3620,4488);
	        bornPosition[10] = new Vector2(4100,5000);

	 
	        bornPosition[11] = new Vector2(4477,4407);
	        bornPosition[12] = new Vector2(4341,3386);
	        bornPosition[13] = new Vector2(3000,3000);
	        bornPosition[14] = new Vector2(4000,2681);
	        bornPosition[15] = new Vector2(3970,2030);
	        bornPosition[16] = new Vector2(836,3573);
	        bornPosition[17] = new Vector2(902,3346);
	        bornPosition[18] = new Vector2(1563,2491);
	        bornPosition[19] = new Vector2(1520,2491);
	        bornPosition[20] = new Vector2(1581,2491);
	        

	        bornPosition[21] = new Vector2(1581,2458);
	        bornPosition[22] = new Vector2(1581,2529);
	        bornPosition[23] = new Vector2(1808,805);
	        bornPosition[24] = new Vector2(1860,805);
	        bornPosition[25] = new Vector2(4000,2000);
	        bornPosition[26] = new Vector2(6212,2175);
	        bornPosition[27] = new Vector2(6890,2163);
	        bornPosition[28] = new Vector2(5830,3769);
	        bornPosition[29] = new Vector2(5155,3715);
	        bornPosition[30] = new Vector2(7279,2975);
	        

	        bornPosition[31] = new Vector2(6759,1835);
	        bornPosition[32] = new Vector2(5825,841);
	        bornPosition[33] = new Vector2(6293,699);
	        bornPosition[34] = new Vector2(4335,5434);
	        bornPosition[35] = new Vector2(5019,7054);
	        bornPosition[36] = new Vector2(6294,6429);
	        bornPosition[37] = new Vector2(6619,7289);
	        bornPosition[38] = new Vector2(5729,4029);
	        bornPosition[39] = new Vector2(7319,6428);
	        bornPosition[40] = new Vector2(2228,3162);
	        
	        bornPosition[41] = new Vector2(2127,3731);
	        
        	lives = MAXLIVES;
        	isLive = true;
        	NO = num;
        	
        	//生成位置
        	setPosition(bornPosition[num].x, bornPosition[num].y);
        	
        	//随机性格
        	isScary = (randomInRange <= 30) ? true : false;//少部分害怕
        	isAngry = (randomInRange <= 10) ? true : false;//少部分愤怒
			System.out.println(NO);
        	return true;
    	}
		return false;
	}
	
	
}
