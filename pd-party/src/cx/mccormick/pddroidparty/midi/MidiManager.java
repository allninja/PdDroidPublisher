package cx.mccormick.pddroidparty.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.puredata.core.PdBase;

import android.content.Context;
import cx.mccormick.pddroidparty.midi.ip.IPMidiDevice;
import cx.mccormick.pddroidparty.midi.nmj.NMJMidiDevice;
import cx.mccormick.pddroidparty.midi.pd.PdMidiDevice;
import cx.mccormick.pddroidparty.midi.pd.PdMidiOutput;

public class MidiManager
{
	private List<MidiDevice> devices;
	
	private IPMidiDevice ipMidiDevice;
	
	private MidiClock clock;
	private MidiInput midiIn;
	private List<MidiOutput> midiOutputs = new ArrayList<MidiOutput>();
	Context ctx;
	private int bpm;
	
	public UsbMidiManager usbMidiManager;
	
	public void init(final Context ctx, UsbMidiManager usbMidiManager, int bpm)
	{
		this.ctx = ctx;
		this.usbMidiManager = usbMidiManager;
		this.bpm = bpm;
		this.ipMidiDevice = new IPMidiDevice();
		
		PdMidiDevice pdMidiDevice = new PdMidiDevice();
		
		devices = new ArrayList<MidiDevice>();
		devices.add(pdMidiDevice);
		devices.add(new NMJMidiDevice());
		devices.add(ipMidiDevice);
		
		for(MidiDevice device : devices)
		{
			device.init(ctx);
		}
		
        clock = new MidiClock();
        clock.init();

        // open first PureData Output
		open(pdMidiDevice.getOutputs().get(0));
	}
	
	

	public void startClock() {
		clock.start(bpm);
	}

	public void stopClock() {
		clock.stop();
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
		clock.setBPM(bpm);
		
	}

	public void setIn(MidiInput newMidiInput) 
	{
		if(midiIn != newMidiInput)
		{
			if(midiIn != null)
			{
				midiIn.close();
				midiIn = null;
			}
			if(newMidiInput != null)
			{
				// stop the clock (switch to slave mode)
				clock.stop();
				
				midiIn = newMidiInput;
				midiIn.open(new MidiListener() {
					@Override
					public void onMidiMessage(byte[] message) {
				    	for(byte d : message)
				    	{
				    		int result = PdBase.sendSysRealTime(0, d & 0xFF);
				    		if(result < 0)
				    		{
				    			System.out.println("no supported message ...");
				    		}
				    	}
					}
				});
			}
		}
	}

	public void resumeClock() 
	{
		clock.resume(bpm);
	}


	public List<MidiDevice> getDeveices() 
	{
		return devices;
	}


	public MidiInput getInput() 
	{
		return midiIn;
	}


	public IPMidiDevice getIPMidiDevice() {
		return ipMidiDevice;
	}

	public boolean isOpened(MidiOutput midiOutput) 
	{
		return midiOutputs.contains(midiOutput);
	}



	public void open(MidiOutput midiOutput) 
	{
		midiOutput.open();
		midiOutputs.add(midiOutput);
		updateOutputs();
	}



	public void close(MidiOutput midiOutput) 
	{
		midiOutput.close();
		midiOutputs.remove(midiOutput);
		updateOutputs();
	}

	private void updateOutputs() 
	{
		List<MidiOutput> internals = new ArrayList<MidiOutput>();
		List<MidiOutput> externals = new ArrayList<MidiOutput>();
		
		for(MidiOutput out : midiOutputs)
		{
			if(out instanceof PdMidiOutput)
			{
				internals.add(out);
			}
			else
			{
				externals.add(out);
			}
		}
		
		clock.setInternals(internals.toArray(new MidiOutput[]{}));
		clock.setExternals(externals.toArray(new MidiOutput[]{}));
	}



	public void setOffsetMs(int value) 
	{
		clock.setOffset(TimeUnit.MILLISECONDS.toNanos(value));
	}

}