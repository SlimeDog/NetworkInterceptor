# NetworkInterceptor
Bukkit plugin to monitor and block outgoing network requests

### Why?
I have a issue with:

* plugins which automatically update themselves
* plugins which constantly nag you to update to the latest version
* plugins which download and execute arbitrary code at runtime from unknown or untrustworthy sources
* plugins which report personal and identifiable information to metrics services without asking users or letting them opt-out

NetworkInterceptor installs a custom security manager into the Java runtime environment which logs (and optionally blocks/prevents) outgoing network connections.

It lets server admins easily monitor the nature of connections made by plugins, and if they want to, prevent them from occurring.

### Can it be bypassed?
A plugin with malicious intent could (most likely) find a way to bypass it, yes.

### More detailed analysis
For more detailed analysis of network traffic, I recommend trying [Wireshark](https://www.wireshark.org/).