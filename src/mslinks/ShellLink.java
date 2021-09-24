/*
	https://github.com/BlackOverlord666/mslinks
	
	Copyright (c) 2015 Dmitrii Shamrikov

	Licensed under the WTFPL
	You may obtain a copy of the License at
 
	http://www.wtfpl.net/about/
 
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/
package mslinks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import io.ByteReader;
import io.ByteWriter;
import mslinks.data.LinkFlags;
import mslinks.extra.ConsoleData;
import mslinks.extra.ConsoleFEData;
import mslinks.extra.EnvironmentVariable;
import mslinks.extra.Stub;
import mslinks.extra.Tracker;
import mslinks.extra.VistaIDList;

public class ShellLink {

	public static final String VERSION = "1.0.7";
	
	private static HashMap<Integer, Class<? extends Serializable>> extraTypes = createMap();
	private static HashMap<Integer, Class<? extends Serializable>> createMap() {
	  HashMap<Integer, Class<? extends Serializable>> result = new HashMap<>();
    result.put(ConsoleData.signature, ConsoleData.class);
    result.put(ConsoleFEData.signature, ConsoleFEData.class);
    result.put(Tracker.signature, Tracker.class);
    result.put(VistaIDList.signature, VistaIDList.class);
    result.put(EnvironmentVariable.signature, EnvironmentVariable.class);
    return result;
	}
	
	/*
	private static HashMap<Integer, Class<? extends Serializable>> extraTypes = new HashMap<>(Map.of(
		ConsoleData.signature, ConsoleData.class,
		ConsoleFEData.signature, ConsoleFEData.class,
		Tracker.signature, Tracker.class,
		VistaIDList.signature, VistaIDList.class,
		EnvironmentVariable.signature, EnvironmentVariable.class
	));
	*/
	
	private ShellLinkHeader header;
	private LinkTargetIDList idlist;
	private LinkInfo info;
	private String name;
	private String relativePath;
	private String workingDir;
	private String cmdArgs;
	private String iconLocation;
	private HashMap<Integer, Serializable> extra = new HashMap<>();
	
	private Path linkFileSource;
	
	public ShellLink() {
		header = new ShellLinkHeader();
		header.getLinkFlags().setIsUnicode();
	}
	
	public ShellLink(String file) throws IOException, ShellLinkException {
		this(Paths.get(file));
	}
	
	public ShellLink(File file) throws IOException, ShellLinkException {
		this(file.toPath());
	}
	
	public ShellLink(Path file) throws IOException, ShellLinkException {
		this(Files.newInputStream(file));
		linkFileSource = file.toAbsolutePath();
	}
	
	public ShellLink(InputStream in) throws IOException, ShellLinkException {
		try (ByteReader reader = new ByteReader(in)) {
			parse(reader);
		}
	}
	
	private void parse(ByteReader data) throws ShellLinkException, IOException {
		header = new ShellLinkHeader(data);
		LinkFlags lf = header.getLinkFlags();
		if (lf.hasLinkTargetIDList()) 
			idlist = new LinkTargetIDList(data);
		if (lf.hasLinkInfo())
			info = new LinkInfo(data);
		if (lf.hasName())
			name = data.readUnicodeString();
		if (lf.hasRelativePath())
			relativePath = data.readUnicodeString();
		if (lf.hasWorkingDir()) 
			workingDir = data.readUnicodeString();
		if (lf.hasArguments()) 
			cmdArgs = data.readUnicodeString();
		if (lf.hasIconLocation())
			iconLocation = data.readUnicodeString();
		
		while (true) {
			int size = (int)data.read4bytes();
			if (size < 4) break;
			int sign = (int)data.read4bytes();
			try {
				Class<?> cl = extraTypes.get(sign);
				if (cl != null)
					extra.put(sign, (Serializable)cl.getConstructor(ByteReader.class, int.class).newInstance(data, size));
				else
					extra.put(sign, new Stub(data, size, sign));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException	| SecurityException e) {	
				e.printStackTrace();
			}
		}
	}
		
	public void serialize(OutputStream out) throws IOException {
		LinkFlags lf = header.getLinkFlags();
		ByteWriter bw = new ByteWriter(out);
		header.serialize(bw);
		if (lf.hasLinkTargetIDList())
			idlist.serialize(bw);
		
		if (lf.hasLinkInfo())
			info.serialize(bw);
		if (lf.hasName())
			bw.writeUnicodeString(name);
		if (lf.hasRelativePath())
			bw.writeUnicodeString(relativePath);
		if (lf.hasWorkingDir()) 
			bw.writeUnicodeString(workingDir);
		if (lf.hasArguments()) 
			bw.writeUnicodeString(cmdArgs);
		if (lf.hasIconLocation())
			bw.writeUnicodeString(iconLocation);
		
		for (Serializable i : extra.values())
			i.serialize(bw);
		
		bw.write4bytes(0);
		out.close();
	}
	
	public ShellLinkHeader getHeader() { return header; }
	
	public LinkInfo getLinkInfo() { return info; }
	public LinkInfo createLinkInfo() {
		info = new LinkInfo();
		header.getLinkFlags().setHasLinkInfo();
		return info;
	}
	public ShellLink removeLinkInfo() {
		info = null;
		header.getLinkFlags().clearHasLinkInfo();
		return this;
	}

	public LinkTargetIDList getTargetIdList() { return idlist; }
	public LinkTargetIDList createTargetIdList() {
		if (idlist == null) {
			idlist = new LinkTargetIDList();
			header.getLinkFlags().setHasLinkTargetIDList();
		}
		return idlist;
	}
	public ShellLink removeTargetIdList() {
		idlist = null;
		header.getLinkFlags().clearHasLinkTargetIDList();
		return this;
	}
	
	public String getName() { return name; }
	public ShellLink setName(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasName();
		else 
			header.getLinkFlags().setHasName();
		name = s;
		return this;
	}
	
	public String getRelativePath() { return relativePath; }
	public ShellLink setRelativePath(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasRelativePath();
		else { 
			header.getLinkFlags().setHasRelativePath();
			if (!s.startsWith("."))
				s = ".\\" + s;
		}
		relativePath = s;
		return this;
	}
	
	public String getWorkingDir() { return workingDir; }
	public ShellLink setWorkingDir(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasWorkingDir();
		else {
			header.getLinkFlags().setHasWorkingDir();
		}
		workingDir = s;
		return this;
	}
	
	public String getCMDArgs() { return cmdArgs; }
	public ShellLink setCMDArgs(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasArguments();
		else 
			header.getLinkFlags().setHasArguments();
		cmdArgs = s;
		return this;
	}
	
	public String getIconLocation() { return iconLocation; }
	public ShellLink setIconLocation(String s) {
		if (s == null) 
			header.getLinkFlags().clearHasIconLocation();
		else {
			header.getLinkFlags().setHasIconLocation();
		}
		iconLocation = s;
		return this;
	}

	public String getLanguage() { 
		return getConsoleFEData().getLanguage();
	}
	
	public ShellLink setLanguage(String s) { 
		getConsoleFEData().setLanguage(s);
		return this;
	}
	
	public ConsoleData getConsoleData() {
		return (ConsoleData)getExtraDataBlock(ConsoleData.signature);
	}
	public ShellLink removeConsoleData() {
		extra.remove(ConsoleData.signature);
		return this;
	}

	public ConsoleFEData getConsoleFEData() {
		return (ConsoleFEData)getExtraDataBlock(ConsoleFEData.signature);
	}
	public ShellLink removeConsoleFEData() {
		extra.remove(ConsoleFEData.signature);
		return this;
	}

	public EnvironmentVariable getEnvironmentVariable() {
		return (EnvironmentVariable)getExtraDataBlock(EnvironmentVariable.signature);
	}
	public ShellLink removeEnvironmentVariable() {
		extra.remove(EnvironmentVariable.signature);
		return this;
	}

	public Tracker getTracker() {
		return (Tracker)getExtraDataBlock(Tracker.signature);
	}
	public ShellLink removeTracker() {
		extra.remove(Tracker.signature);
		return this;
	}

	public VistaIDList getVistaIDList() {
		return (VistaIDList)getExtraDataBlock(VistaIDList.signature);
	}
	public ShellLink removeVistaIDList() {
		extra.remove(VistaIDList.signature);
		return this;
	}

	/**
	 * linkFileSource is the location where the lnk file is stored
	 * used only to build relative path and is not serialized
	 */
	public Path getLinkFileSource() { return linkFileSource; }
	public ShellLink setLinkFileSource(Path path) {
		linkFileSource = path;
		return this;
	}
	
	public String resolveTarget() {
		if (header.getLinkFlags().hasLinkTargetIDList() && idlist != null && idlist.isCorrect()) {
			return idlist.buildPath();
		}
		
		if (header.getLinkFlags().hasLinkInfo() && info != null) {
			String path = info.buildPath();
			if (path != null)
				return path;
		}
		
		if (linkFileSource != null && header.getLinkFlags().hasRelativePath() && relativePath != null) 
			return linkFileSource.resolveSibling(relativePath).normalize().toString();

		EnvironmentVariable envBlock = (EnvironmentVariable)extra.get(EnvironmentVariable.signature);
		if (envBlock != null && !envBlock.getVariable().isEmpty())
			return envBlock.getVariable();
		
		return "<unknown>";
	}

	private Serializable getExtraDataBlock(int signature) {
		Serializable block = extra.get(signature);
		if (block == null) {
			Class<?> type = extraTypes.get(signature);
			try {
				block = (Serializable)type.getConstructor().newInstance();
				extra.put(signature, block);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException	| SecurityException e) {
				e.printStackTrace();
			}
		}
		return block;
	}
	

}
