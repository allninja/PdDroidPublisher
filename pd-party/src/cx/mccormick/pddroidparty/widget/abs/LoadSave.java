package cx.mccormick.pddroidparty.widget.abs;

import java.util.ArrayList;
import java.util.List;

import org.puredata.core.PdBase;

import cx.mccormick.pddroidparty.PdDroidParty;
import cx.mccormick.pddroidparty.view.PdDroidPatchView;
import cx.mccormick.pddroidparty.widget.Widget;

public class LoadSave extends Widget {
	private PdDroidPatchView parent = null;
	private String filename = "";
	private String directory = ".";
	private String extension = "";
	private String sendreceive = null;
	
	public LoadSave(PdDroidPatchView app, String[] atomline) {
		super(app);
		parent = app;
		sendreceive = atomline[5];
		app.registerReceiver(sendreceive, this);
	}
	
	public String getFilename() {
		return filename;
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public String getExtension() {
		return extension;
	}
	
	public void receiveMessage(String symbol, Object... args) {
		int type = 0;
		if (symbol.equals("save")) {
			type = PdDroidParty.DIALOG_SAVE;
		} else if (symbol.equals("load")) {
			type = PdDroidParty.DIALOG_LOAD;
		}
		directory = args.length > 0 ? (String)args[0] : ".";
		extension = args.length > 1 ? (String)args[1] : "";
		parent.launchDialog(this, type);
	}
	
	public void gotFilename(String type, String newname) {
		filename = newname;
		List<Object> details = new ArrayList<Object>();
		details.add(parent.getPatchRelativePath(directory));
		details.add(filename);
		details.add(extension);
		Object[] ol = details.toArray();
		PdBase.sendList(sendreceive + "-" + type + "-detail", ol);
		PdBase.sendSymbol(sendreceive + "-" + type, parent.getPatchRelativePath(directory) + "/" + filename + "." + extension);
	}
}
