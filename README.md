# Mixin tutorial
Using Mixins to Mod Minecraft Vanilla.

# Setup
Download the build.gradle in this repository and open it in IntelliJ IDEA.

Before you do anything, make sure that you run the `wrapper` task, and go into gradle-wrapper.properties and change the number to 4.7. ForgeGradle doesn't work with newer versions of Gradle.

Now, edit the following:

```gradle

version = "0.1"
group = "com.exampleClient"
archivesBaseName = "ExampleClient"

```

`version` is the version of your client, pretty self explanatory.
`group` is your base package.
`archivesBaseName` should be the name of your Client.

```gradle
minecraft {
    version = "1.8.9"
    tweakClass = "exampleclient.launch.ExampleTweaker"
    mappings = "stable_22"
    runDir = 'run'
    clientJvmArgs = ["-XX:-DisableExplicitGC"] // fast world loading
    makeObfSourceJar = false
}
```

You can leave everything the same except for `tweakClass`.

```gradle
mixin {
    defaultObfuscationEnv notch
    add sourceSets.main, "mixin.exampleClient.refmap.json"
}
```

change `exampleClient` to your client name.

```gradle
manifest.attributes(
		"MixinConfigs": 'mixins.exampleClient.json',
		"TweakClass": 'com.exampleClient.launch.ExampleClientTweaker',
		"TweakOrder": 0,
		"Manifest-Version": 1.0
)
```

Change the MixinConfigs to your .json file and change your TweakClass to your Tweaker.
You're all set!

# How to Create a Tweaker

(NOTE: This is taken from Hyperium.)

Create a class (whatever name you want) and implement ITweaker. Look at the code below.

```java
public class ClientTweaker implements ITweaker {

    private static final Logger logger = LogManager.getLogger();

    private ArrayList<String> args = new ArrayList<>();

    @Override
    public void acceptOptions(List<String> args, File gameDir, final File assetsDir, String profile) {
        this.args.addAll(args);

        addArg("gameDir", gameDir);
        addArg("assetsDir", assetsDir);
        addArg("version", profile);
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        logger.info("Initializing Bootstraps...");
        MixinBootstrap.init();
        logger.info("Adding mixin configuration...");
        MixinEnvironment environment = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.example.json");

        if (environment.getObfuscationContext() == null) {
            environment.setObfuscationContext("notch"); // Switch's to notch mappings
        }

        environment.setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String[] getLaunchArguments() {
        return args.toArray(new String[]{});
    }

    private void addArg(String label, Object value) {
        args.add("--" + label);
        args.add(value instanceof String ? (String) value : value instanceof File ? ((File) value).getAbsolutePath() : ".");
    }
}
```
 
The main part we want to focus on is the function `injectIntoClassLoader`.

We are first initializing the MixinBootstrap. Then, we are adding our Mixin configuration. We are also switching the obfuscation context to notch's mappings.



# MIXIN Cheatsheet

What mixins should you use, for what purpose, and how exactly do they affect the code?

_Note: the method modifications listed in these documents are what the code_ effectively _does, not what the modified bytecode will actually look like. Mixin generates additional methods in the target classes that are inlined here._

(*This Cheatsheet is originally from [(2xsaiko)](https://github.com/2xsaiko/mixin-cheatsheet)

## Table of Contents

### Injectors
 - [`@Inject`](inject.md)
 - [`@Inject`, cancellable](inject-cancellable.md)
 - [`@Inject`, locals](inject-locals.md)
 - [`@Redirect`](redirect.md)
 - [`@Overwrite`](overwrite.md)
 - [`@ModifyArg`](modify-arg.md)
 - [`@ModifyArgs`](modify-args.md)
 - ~~[`@ModifyConstant`](modify-constant.md)~~
 - [`@ModifyVariable`](modify-variable.md)

 ### Non-Injectors
  - [`@Invoker`](invoker.md)
  - [`@Accessor`](accessor.md)
  - [`@Shadow`](shadow.md)
  - [`@Shadow`, final](shadow-final.md)
  - [`@Shadow`, anonymous class](shadow-anonymous.md)
  - [`@At`](at.md)
  - [`@Unique`](unique.md)

### Other helpful docs
  - [Changing your mixin's priority](mixin-priority.md)
  - [Mixing into classes that may not exist at runtime](pseudo-mixin.md)


# MIXINS 

Mixins are a way of modifying java code at runtime by adding additional behavior to classes. They enable transplanting of intended behavior into existing Minecraft objects. Mixins are necessary for all official Sponge implementations to function.

A basic introduction to some of the core concepts underpinning the mixin functionality we’re using to implement Sponge is available at the [(Mixin Wiki)](https://github.com/SpongePowered/Mixin/wiki/).

# Mixins and Inner Classes

While you can use lambdas, anonymous and inner classes inside mixins, you cannot use an anonymous/inner class within another anonymous/inner class that is also inside a mixin, unless one of the inner classes is static.

This means expressions like the following will cause mixins to fail horribly and bring death and destruction upon all that attempt to use Sponge.

```javaa
return new Collection<ItemStack>() {
    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<ItemStack>() {
            // Not working

            @Override
            public boolean hasNext() {
                // Write your code
            }

            @Override
            public ItemStack next() {
                // Write your code
            }
        };
    }

    // Write your other methods 
};
```
This applies to all classes that are annotated with @Mixin. Classes that are not touched by the mixin processor may make use of those features. There are two ways to work around this.

One option is to use a separate utility class, as unlike your mixin class that utility class will still exist at runtime, while your mixin class will be merged into the specified target class. The following code therefore will work.

```java
public class SampleCollection implements Collection<ItemStack> {

    private final TargetClass target;

    public SampleCollection(TargetClass target) {
        this.target = target;
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return new Iterator<ItemStack>() {

            @Override
            public boolean hasNext() {
                // Write your code
            }

            @Override
            public ItemStack next() {
                //  Write your code
            }
        };
    }

    // Other methods skipped
}

@Mixin(TargetClass.class)
public abstract class SomeMixin {
    public Collection<ItemStack> someFunction() {
        return new SampleCollection((TargetClass) (Object) this);
    }
}
```
The other option is simply to place all of the nested inner classes directly into the mixin or one of its methods, as opposed to one inside the other. For example:

```java
@Mixin(TargetClass.class)
public abstract class SomeMixin {

    private final class SampleIterator implements Iterator<ItemStack> {

        @Override
        public boolean hasNext() {
            // Write your code
        }

        @Override
        public ItemStack next() {
            // Write your code
        }
    }

    public Collection<ItemStack> someFunction() {
        return new Collection<ItemStack>() {
            @Override
            public Iterator<ItemStack> iterator() {
                return new SampleIterator();
            }

            // Write your other methods
        };
    }
}
```
These are basics of SpongePowered mixins.You can find more about it in their documentations.
Resources
More info on Mixins can be found with these links:
Javadocs: https://jenkins.liteloader.com/view/Other/job/Mixin/javadoc/
Wiki: https://github.com/SpongePowered/Mixin/wiki
Discord: https://discord.gg/sponge
