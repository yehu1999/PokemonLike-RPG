package theScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import java.util.Random;

import theEntities.Player;
import theEntities.Pokemon;
import theEntities.Laser;

/**
 * 游戏场景
 * yehu1999
 */
public class PlayScreen implements Screen {

	private TiledMap map;                        //地图
	private OrthogonalTiledMapRenderer renderer; //地图渲染器 //IsometricTiledMapRenderer
	private ShapeRenderer objectRenderer;        //MapObject渲染器
	private OrthographicCamera camera;           //相机
	
	private Player player;//玩家
	private TextureAtlas playerAtlas;//玩家图像集
	private float oldView;   //玩家视野存值

	private LinkedList<Pokemon> pokemons;                         //宝可梦对象链表
	public static final int POKEMON_SPECIES_NUM = 42;             //宝可梦种类数量 
	public static final int POKEMON_NUM = 42;                     //宝可梦最大数量 
	private int pokemonNum = 0;                                   //当前宝可梦数量
	private TextureAtlas pokemonsAtlas[] = new TextureAtlas[99];  //宝可梦图像集
	private Animation Pup[] = new Animation[POKEMON_NUM],         //宝可梦动画资源
					Pdown[] = new Animation[POKEMON_NUM], 
					Pleft[] = new Animation[POKEMON_NUM], 
					Pright[] = new Animation[POKEMON_NUM];
	
	BitmapFont font;                                           //字体
	private int foundNum = 0;                                  //已发现的宝可梦数量
	private int oldFoundNum = 0;                               //上一次已发现的宝可梦数量
	
	private Sound scoreSound;                                  //得分音效
	private Sound backgroundSound;                             //背景音乐
	private Sound caveSound;                                   //洞穴音乐
	
	private LinkedList<Laser> lasers;                          //镭射对象链表
	public static final int LASER_NUM = 100;                   //镭射最大数量
	
	@Override
	public void show() {
		
		//加载字体
		font = new BitmapFont();
		font.setColor(Color.BLUE);
		font.getData().setScale(1.2f);
		
		//加载音效
		scoreSound = Gdx.audio.newSound(Gdx.files.internal("audio/score.ogg"));
		backgroundSound = Gdx.audio.newSound(Gdx.files.internal("audio/background.ogg"));
		//caveSound  = Gdx.audio.newSound(Gdx.files.internal("audio/JigglypuffSong.mp3"));
		long soundId = backgroundSound.play(0.3f);
		backgroundSound.setLooping(soundId, true);
		
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
		
		//玩家动画
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
		
		//宝可梦动画
		for(int i = 0; i < POKEMON_SPECIES_NUM; i++) {//加载所有种类宝可梦的动画资源
			pokemonsAtlas[i] = new TextureAtlas("img/pokemons/" + i + ".pack");
			
			Pup[i] = new Animation<TextureRegion>(1 / 6f, pokemonsAtlas[i].findRegions("up"));
			Pdown[i] = new Animation<TextureRegion>(1 / 6f, pokemonsAtlas[i].findRegions("down"));
			Pleft[i] = new Animation<TextureRegion>(1 / 6f, pokemonsAtlas[i].findRegions("left"));
			Pright[i] = new Animation<TextureRegion>(1 / 6f, pokemonsAtlas[i].findRegions("right"));
			Pup[i].setPlayMode(Animation.PlayMode.LOOP);
			Pdown[i].setPlayMode(Animation.PlayMode.LOOP);
			Pleft[i].setPlayMode(Animation.PlayMode.LOOP);
			Pright[i].setPlayMode(Animation.PlayMode.LOOP);
		}
		
		//玩家对象
		player = new Player(still, up, down, left, right, (TiledMapTileLayer) map.getLayers().get(0));
		player.setPosition(player.POISTION_X * player.getCollisionLayer().getTileWidth(), (player.getCollisionLayer().getHeight() - player.POISTION_Y) * player.getCollisionLayer().getTileHeight());
		oldView = player.view;
		
		//宝可梦对象链表
		pokemons = new LinkedList<>();
		for(int i = 0; i < POKEMON_NUM; i++) {
			pokemons.add(new Pokemon(Pup[i], Pdown[i], Pleft[i], Pright[i], (TiledMapTileLayer) map.getLayers().get(0)));
		}
		
		//镭射对象链表
		lasers = new LinkedList<>();
		
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
		int ViewBeginX = (int) (player.getX() / tileWidth)  - 300;
		ViewBeginX = (ViewBeginX > 0) ? ViewBeginX : 0;
		int ViewEndX = ViewBeginX + 600;
		int ViewBeginY = (int) (player.getY() / tileHeight) - 300;
		ViewBeginY = (ViewBeginY > 0) ? ViewBeginY : 0;
		int ViewEndY = ViewBeginY + 600;
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
		//相关更新
		update();
		
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
		
		//渲染前景地图
		renderer.renderTileLayer((TiledMapTileLayer)map.getLayers().get("foreground"));
		
		//渲染宝可梦
		ListIterator<Pokemon> iterator = pokemons.listIterator();
		foundNum = 0;
		while (iterator.hasNext()) {
        	Pokemon poke = iterator.next();
        	if(poke.isLive) {
        		poke.draw(renderer.getBatch());
        	} 
        	if(poke.isFound) {
        		foundNum++;
        	}
        }
		
		//加分音效
		if(oldFoundNum < foundNum) {
			scoreSound.play(0.3f);
			oldFoundNum = foundNum;
		}
		
		
		//渲染玩家
		player.draw(renderer.getBatch());
		
		//渲染镭射
		ListIterator<Laser> laserIterator = lasers.listIterator();
		while (iterator.hasNext()) {
			Laser laser = laserIterator.next();
        	if(laser.isLive) {
        		laser.draw(renderer.getBatch());
        	} 
        }
		
		//渲染迷雾地图
		renderer.renderTileLayer((TiledMapTileLayer)map.getLayers().get("smog"));
		
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
		
		//渲染文字
	    font.draw(renderer.getBatch(), "FoundPokemons: " + foundNum + "/" + POKEMON_NUM, player.getX() - 300, player.getY() + 190);
		
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
		lasers.clear();
		pokemons.clear();
		objectRenderer.dispose();
	}
	
	public void update() {
		
		//应用宝可梦AI
		for(Pokemon pokemon : pokemons) {
			pokemon.SimpleAI(player.getX(),player.getY());
		}
		
		ListIterator<Pokemon> iterator = pokemons.listIterator();
        while (iterator.hasNext()) {
        	Pokemon poke = iterator.next();
    		//生成宝可梦
        	if(poke.generate(pokemonNum, player.meetOdd)) {
        		pokemonNum++;
        	}
        	
            //消灭宝可梦
        	if(poke.lives <= 0) {
        		poke.isLive = false;
        		pokemonNum--;
        	}
        }     
	}
}
