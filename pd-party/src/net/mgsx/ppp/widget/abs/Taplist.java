package net.mgsx.ppp.widget.abs;

import java.util.ArrayList;
import java.util.List;

import net.mgsx.ppp.pd.PdHelper;
import net.mgsx.ppp.view.PdDroidPatchView;
import net.mgsx.ppp.widget.Widget;

import org.puredata.core.PdBase;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class Taplist extends Widget {
	private static final String TAG = "Taplist";

	String longest = null;
	ArrayList<String> atoms = new ArrayList<String>();

	WImage on = new WImage();
	WImage off = new WImage();

	boolean down = false;
	int pid0 = -1; //pointer id when down
	
	public Taplist(PdDroidPatchView app, String[] atomline) {
		super(app);

		float x = Float.parseFloat(atomline[2]) ;
		float y = Float.parseFloat(atomline[3]) ;
		float w = Float.parseFloat(atomline[5]) ;
		float h = Float.parseFloat(atomline[6]) ;
		
		label = atomline[9];

		fontsize = (int) (h * 0.75); // XXX Wrong !

		// get list atoms
		for (int a = 10; a < atomline.length; a++) {
			atoms.add(atomline[a]);
		}

		paint.setTextSize(fontsize);
		paint.setTextAlign(Paint.Align.CENTER);

		sendname = app.replaceDollarZero(atomline[8]);
		receivename = atomline[7];

		setval(0, 0);

		// graphics setup
		dRect = new RectF(Math.round(x), Math.round(y), Math.round(x + w),
				Math.round(y + h));

		// listen out for floats from Pd
		setupreceive();
		
		if (isValidSymbolName(receivename)) {
			parent.registerReceiver(receivename + "/idx", this);
		}


		// try and load images
		on.load(TAG, "on", label, sendname, receivename);
		off.load(TAG, "off", label, sendname, receivename);

		setTextParametersFromSVG(on.svg);
		setTextParametersFromSVG(off.svg);
	}

	public void draw(Canvas canvas) {
		if (down) {
			paint.setStrokeWidth(2);
		} else {
			paint.setStrokeWidth(1);
		}

		if (down ? on.draw(canvas) : off.draw(canvas)) {
			paint.setStyle(Style.FILL);
			paint.setColor(bgcolor);
			canvas.drawRect(dRect, paint);
			
			paint.setStyle(Style.STROKE);
			paint.setColor(fgcolor);
			canvas.drawRect(dRect, paint);
		}
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(fgcolor);
		drawCenteredText(canvas, atoms.get((int) val));
	}

	public void doSend() {
		PdBase.sendFloat(sendname + "/idx", val);
		PdHelper.send(sendname, atoms.get((int) val));
	}

	public boolean touchdown(int pid, float x,float y) {
		if (dRect.contains(x, y)) {
			val = (val + 1) % atoms.size();
			doSend();
			down = true;
			pid0 = pid;
			return true;
		}
		return false;
	}
	
	public boolean touchup(int pid, float x,float y) {
		if (pid == pid0) {
			down = false;
			pid0 = -1;
			return true;
		}
		return false;
	}
	
	public void receiveList(Object... args) {
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float) args[0]);
		}
	}
	
	public void receiveFloat(float v) {
		val = ((v % atoms.size()) + atoms.size()) % atoms.size();
		doSend();
	}
	
	public void receiveMessage(String symbol, Object... args) {
		if(widgetreceiveSymbol(symbol,args)) return;
		if(args.length == 0)
		{
			int index = atoms.indexOf(symbol);
			if(index >= 0)
			receiveFloat(index);
		}
		if (args.length > 0 && args[0].getClass().equals(Float.class)) {
			receiveFloat((Float)args[0]);
		}
	}
	
	public List<String> getValues(){	
		return atoms;
	}
	
}
