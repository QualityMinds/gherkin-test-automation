package de.qualityminds.gta.driver.ssh;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import org.jetbrains.annotations.NotNull;

@Getter
public class LsEntry implements Comparable<LsEntry> {
	private static final Pattern ownerGroupRegexp = Pattern.compile("[drwsx-]{10}\\s*\\d*\\s*(\\S+)\\s+(\\S+)\\s+.*");
	private final String permissionString;

	private final String owner;
	private final int uid;

	private final String group;
	private final int gid;

	private final String size;
	private final String modDateTime;

	private final boolean isDir;
	private final String filename;

	public LsEntry(ChannelSftp.LsEntry lse) {
		this.filename = lse.getFilename();

		SftpATTRS attrs = lse.getAttrs();
		this.permissionString = attrs.getPermissionsString();

		this.size = String.valueOf(attrs.getSize());
		this.modDateTime = attrs.getMtimeString();

		this.uid = attrs.getUId();
		this.gid = attrs.getGId();


		Matcher longNameMatcher = ownerGroupRegexp.matcher(lse.getLongname());
		boolean parseable = longNameMatcher.find();
		if (parseable && longNameMatcher.groupCount() == 2) {
			this.owner = longNameMatcher.group(1);
			this.group = longNameMatcher.group(2);
		} else {
			this.owner = String.valueOf(uid);
			this.group = String.valueOf(gid);
		}

		this.isDir = permissionString != null && permissionString.startsWith("d");
	}

	@Override
	public String toString() {
		return permissionString + "\t" + owner + "\t" + group + "\t" + size + "B\t" + modDateTime + "\t" + filename;
	}

	@Override
	public int compareTo(@NotNull LsEntry o) {
		return this.filename.compareTo(o.getFilename());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LsEntry lsEntry = (LsEntry) o;
		return uid == lsEntry.uid && gid == lsEntry.gid && isDir == lsEntry.isDir && Objects.equals(permissionString, lsEntry.permissionString) && Objects.equals(owner, lsEntry.owner) && Objects.equals(group, lsEntry.group) && Objects.equals(size, lsEntry.size) && Objects.equals(modDateTime, lsEntry.modDateTime) && Objects.equals(filename, lsEntry.filename);
	}

	@Override
	public int hashCode() {
		return Objects.hash(permissionString, owner, uid, group, gid, size, modDateTime, isDir, filename);
	}
}
