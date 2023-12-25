package theScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import theEntities.Enemy;
import theEntities.Player;

/**
 * 游戏场景
 * yehu1999
 */
public class PlayScreen implements Screen {

	private TiledMap map;//地图
	private OrthogonalTiledMapRenderer renderer; //地图渲染器 //IsometricTiledMapRenderer
	private OrthographicCamera camera;//相机
	
	private Player player;//玩家
	private TextureAtlas playerAtlas;//玩家图像集
	private float oldView;//玩家视野存值

	private LinkedList<Enemy> enemys;//敌人
	
	private ShapeRenderer objectRenderer;//MapObject渲染器
	
	@Override
	public void show() {
		TmxMapLoader loader = new TmxMapLoader();
		
		//加载地图tmx文件 
		map = loader.load("maps/Mymap.tmx");//isometric
		
		//map渲染器
		renderer = new OrthogonalTiledMapRenderer(map);//IsometricTiledMapRenderer
		
		//MapObject渲染器
		objectRenderer = new ShapeRenderer();
		objectRenderer.setColor(Color.CYAN);//设置颜色
		Gdx.gl.glLineWidth(3);//设置线宽
		
		//相机
		camera = new OrthographicCamera();
		
		//动画
		playerAtlas = new TextureAtlas("img/myPlayer.pack");
		Animation still, up, down, left, right;
		still = new Animation<TextureRegion>(1 / 2f, playerAtlas.findRegions("still"));
		up = new Animation<TextureRegion>(1 / 6f, playerAtlas.findRegions("up"));
		down = new Animation<TextureRegion>(1 / 6f, playerAtlas.findRegions("down"));
		left = new Animation<TextureRegion>(1 / 6f, playerAtlas.findRegions("left"));
		right = new Animation<TextureRegion>(1 / 6f, playerAtlas.findRegions("right"));
		still.setPlayMode(Animation.PlayMode.LOOP);
		up.setPlayMode(Animation.PlayMode.LOOP);
		down.setPlayMode(Animation.PlayMode.LOOP);
		left.setPlayMode(Animation.PlayMode.LOOP);
		right.setPlayMode(Animation.PlayMode.LOOP);
		
		//玩家对象
		player = new Player(still, up, down, left, right, (TiledMapTileLayer) map.getLayers().get(0));
		player.setPosition(player.POISTION_X * player.getCollisionLayer().getTileWidth(), (player.getCollisionLayer().getHeight() - player.POISTION_Y) * player.getCollisionLayer().getTileHeight());
		oldView = player.view;
		
		//敌人池对象
		enemys = new LinkedList<>();
		
		//设置控制输入
		Gdx.input.setInputProcessor(player);
		
		//动态TileMap
		//获取动态Tile帧
		Array<StaticTiledMapTile> frameTiles = new Array<StaticTiledMapTile>(2);
		Iterator<TiledMapTile> tiles = map.getTileSets().getTileSet("myTiles").iterator();
		while(tiles.hasNext()) {
			TiledMapTile tile = tiles.next();
			if(tile.getProperties().containsKey("animation") 
					&& tile.getProperties().get("animation",String.class).equals("flower"))
			frameTiles.add((StaticTiledMapTile)tile);
		}
		//创建动态Tile
		AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(1 / 3f, frameTiles);
		
		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("background");
		
		
		float tileWidth = layer.getTileWidth();
		float tileHeight = layer.getTileHeight();
		int ViewBeginX = (int) (player.getX() / tileWidth)  - 30;
		ViewBeginX = (ViewBeginX > 0) ? ViewBeginX : 0;
		int ViewEndX = ViewBeginX + 60;
		int ViewBeginY = (int) (player.getY() / tileHeight) - 30;
		ViewBeginY = (ViewBeginY > 0) ? ViewBeginY : 0;
		int ViewEndY = ViewBeginY + 60;
		for(int x = ViewBeginX; x < layer.getWidth() && x < ViewEndX; x++)
			for(int y = ViewBeginY; y < layer.getHeight() && y < ViewEndY; y++) {
				Cell cell = layer.getCell(x, y);
				if(cell.getTile().getProperties().containsKey("animation") &&
						cell.getTile().getProperties().get("animation",String.class).equals("flower"))
					cell.setTile(animatedTile);
						
			}
				
		
	}

	@Override
	public void render(float delta) {
		//渲染
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		
		//相机位置跟随玩家
		camera.position.set(player.getX() + player.getWidth() / 2,player.getY() + player.getHeight() / 2,0);
		//玩家视野变换
		if(player.view != oldView)
		{
			camera.viewportWidth = Gdx.graphics.getWidth() / player.view;
			camera.viewportHeight = Gdx.graphics.getHeight() / player.view;
			oldView = player.view;
		}
		camera.update();
		renderer.setView(camera);
		
		//更新地图动画时间
		AnimatedTiledMapTile.updateAnimationBaseTime();
		
		//开始渲染
		renderer.getBatch().begin();
		
		//渲染背景地图
		renderer.renderTileLayer((TiledMapTileLayer)map.getLayers().get("background"));
		
		//渲染玩家
		player.draw(renderer.getBatch());
		
		//渲染前景地图
		renderer.renderTileLayer((TiledMapTileLayer)map.getLayers().get("foreground"));
		
		//渲染MapObject
		objectRenderer.setProjectionMatrix(camera.combined);//根据相机位置进行渲染
		for(MapObject object : map.getLayers().get("objects").getObjects()) {
			if(object instanceof RectangleMapObject) {
				RectangleMapObject rectObject = (RectangleMapObject) object;
				Rectangle rect = rectObject.getRectangle();
				if(rectObject.getProperties().containsKey("gid")) { // if it contains the gid key, it's an image object from Tiled
					int gid = rectObject.getProperties().get("gid", Integer.class);
					TiledMapTile tile = map.getTileSets().getTile(gid);
					//renderer.getBatch().begin();
					renderer.getBatch().draw(tile.getTextureRegion(), rect.x, rect.y);
					//renderer.getBatch().end();
				} else { // otherwise, it's a normal RectangleMapObject
					objectRenderer.begin(ShapeType.Filled);
					objectRenderer.rect(rect.x, rect.y, rect.width, rect.height);
					objectRenderer.end();
				}
			} else if(object instanceof CircleMapObject) {
				Circle circle = ((CircleMapObject) object).getCircle();
				objectRenderer.begin(ShapeType.Filled);
				objectRenderer.circle(circle.x, circle.y, circle.radius);
				objectRenderer.end();
			} else if(object instanceof EllipseMapObject) {
				Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
				objectRenderer.begin(ShapeType.Filled);
				objectRenderer.ellipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
				objectRenderer.end();
			} else if(object instanceof PolylineMapObject) {
				Polyline line = ((PolylineMapObject) object).getPolyline();
				objectRenderer.begin(ShapeType.Line);
				objectRenderer.polyline(line.getTransformedVertices());
				objectRenderer.end();
			} else if(object instanceof PolygonMapObject) {
				Polygon poly = ((PolygonMapObject) object).getPolygon();
				objectRenderer.begin(ShapeType.Line);
				objectRenderer.polygon(poly.getTransformedVertices());
				objectRenderer.end();
			}
		}
		
		
		
		//结束渲染
		renderer.getBatch().end();
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width / player.view;
		camera.viewportHeight = height / player.view;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		dispose();

	}

	@Override
	public void dispose() {
		map.dispose();
		renderer.dispose();
		player.getTexture().dispose();
		playerAtlas.dispose();
		objectRenderer.dispose();
	}

}
