### Overview
**NetworkInterceptor** detects and (optionally) blocks outgoing network connections. Some examples include:
* Generally required:
  * player authentication (Mojang)
* Generally good:
  * server version check (Minecraft variants: Paper, Spigot, etc.)
  * plugin version check
* Generally not so good:
  * plugin auto-download and install
  * plugin arbitrary code download and execute
  * plugin data reporting

**NetworkInterceptor** installs a custom security manager into the Java runtime environment which logs and (optionally) blocks outgoing network connections.
This allows server administrators easily to monitor the nature of connections made by plugins, and if they desire, prevent them.

### Can it be bypassed?
A plugin with malicious intent could (most likely) find a way to bypass it.

### More detailed analysis
For more detailed analysis of network traffic, we recommend trying [Wireshark](https://www.wireshark.org/).

### Commands
All **NetworkInterceptor** commands are accessible at the console, and in-game with appropriate permissions (default OP). Tab-completion is supported for all commands.

### Version Support
**NetworkInterceptor** 3.1.2 is certified for Spigot and Paper 1.16.5 and 1.17.x, BungeeCord and Waterfall 1.17, and Velocity 3.0.0.

**NetworkInterceptor** may or may not work on previous Spigot releases or other non-Spigot variants.

Note: Only certified releases are supported.

### The Wiki
**NetworkInterceptor** is completely documented on The Wiki. Please start there when you have questions.
https://github.com/SlimeDog/NetworkInterceptor/wiki

And we'll never forget: **NetworkInterceptor 1.0** was made with ❤️ by Luck. All credit for the original design and implementation belongs to him.
