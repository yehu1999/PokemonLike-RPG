package theEntities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Laser  extends Sprite{
	
	//宝可梦常量数据
	public static final float SPEED = 60 * 3.0f;             //初始速度
	public static final String blockedKey = "blocked";       //碰撞属性关键字

	//宝可梦变量数据
	private Vector2 velocity = new Vector2(); //速度向量
	private float speed = SPEED;              //速度大小
	private boolean isMove_UP = false, isMove_DOWN = false, isMove_LEFT = false, isMove_RIGHT = false;//移动状态标志
	
	public TiledMapTileLayer backgroundLayer;  //Map背景图层
	private float increment;                   //步长
	
	private Animation<TextureRegion> animationUp ,animationDown , animationLeft, animationRight;      //动画
	float animationTime = 0;                  //动画时间

	public Boolean isLive = false;            //是否存活
	private int aAttackValue = 45;            //伤害

    //position and dimensions
    Rectangle boundingBox;

    //graphics
    TextureRegion textureRegion;

    public Laser(float xCentre, float yBottom, float width, float height, float movementSpeed, TextureRegion textureRegion) {
        this.boundingBox = new Rectangle(xCentre - width / 2, yBottom, width, height);

        this.speed = movementSpeed;
        this.textureRegion = textureRegion;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(textureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
    }

//    public Rectangle getBoundingBox() {
//        return boundingBox;
//    }
}

