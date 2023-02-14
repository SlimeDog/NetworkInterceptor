## NetworkInterceptor <a href="https://www.spigotmc.org/resources/53351/">![download-on-spigot](https://user-images.githubusercontent.com/17748923/187102011-b72e0f1d-ba74-4cb2-a69e-46f48cb364b5.png)</a> <a href="https://hangar.papermc.io/SlimeDog/NetworkInterceptor">![download-on-hangar](https://user-images.githubusercontent.com/17748923/187102194-00e910e6-ee8e-42cb-bfe1-d2f9e657ef4b.png)</a>

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
**NetworkInterceptor** 3.1.5 is certified Spigot 1.13.2, 1.14.4, 1.15.2, 1.16.5, and 1.17.1 and 1.18; Paper 1.16.5, 1.17.x and 1.18; BungeeCord 1.18-1615; Waterfall 1.17-460, and Velocity 3.1.0.

**NetworkInterceptor** may or may not work on previous Spigot releases or other non-Spigot variants.

Note: Only certified releases are supported.

### The Wiki
**NetworkInterceptor** is completely documented on The Wiki. Please start there when you have questions.
https://github.com/SlimeDog/NetworkInterceptor/wiki

And we'll never forget: **NetworkInterceptor 1.0** was made with ❤️ by Luck. All credit for the original design and implementation belongs to him.
