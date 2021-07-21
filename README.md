# Mixin tutorial
Using Mixins to Mod Minecraft Vanilla.

## Setup

Download the build.gradle in this repository and open it in IntelliJ IDEA.

Before you do anything, make sure that you run the `wrapper` task, and go into gradle-wrapper.properties and change the number to 4.7. ForgeGradle doesn't work with newer versions of Gradle.


Now,edit the following:

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
**Detailed Explaination.**

Create a new folder where ever you want your client to be. 

Open up IntelliJ. Then click `New Project > Gradle > Next`. Set the `Location` to the folder you just created


Copy and paste the ![build.gradle](https://github.com/PaceCodes/Mixin-Tutorial-With-Cheatsheet/blob/main/build.gradle) into your `build.gradle`.


Then click the "Load Gradle Changes" Button on the top right.


You might be noticing an error popping up in the bottom half, that's totally normal. It's because forgegradle doesn't support newer versions of gradle.



To fix this, go to `gradle > wrapper > gradle-wrapper.properties` then replace whatever version is currently there, with 4.7





Now for the lengthy part. Replace all the "example" strings with your client name. Also replace the "net" packages with whatever fits you. An example is shown below.

**! This MUST be done for all instances. DO NOT CHANGE ANY OF THE "mixin" OR "mixins" strings !**


Once you are done, click the gradle button on the far right. Then click `Tasks > forgegradle > setupDecompWorkspace`. This will take some time.


Once that process is finished, click `Tasks > forgegradle > genIntellijRuns`


Click the button at the top then click `Minecraft Client` An example is shown below.



Click the same button again, then click `Edit Configurations`. You will see an error saying "Class 'GradleStart not found in module 'ExampleClient'". 
To fix this, set the `Client` to `Client.main`.



Click Apply. The error should be gone now.



## Tweaker

Create a new package in `src/main/java` with the package name you've set in your `build.gradle` in this case, it would be `net.example`

Create a new package named `mixins` inside the previous pacakge then create a new `java` file with whatever you named your tweaker. In this case, it's `ExampleTweaker`. 



**Copy the code from the tweaker below to yours.**

Replace the `example` at `Mixins.addConfiguration("mixins.example.json");` (inside your tweaker) with your client name or whatever you named your package.





## Coding Mixins

Now it's time to make your first mixin!

Create a new file named whatever was inside your `Mixins.addConfiguration("mixins.example.json");` inside the resource folder.



Then, copy the json from ![here]("https://github.com/PaceCodes/Mixin-Tutorial-With-Cheatsheet/blob/main/mixins.example.json") into the json file you just created.

Ok, now it's time to create an actual mixin.

Create a package named `client` inside the package where your tweaker is located.

Then, inside that `client` package, create a java file named `MixinMinecraft`



then code mixins (example):

```java
@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "createDisplay", at = @At("RETURN"))
    public void createDisplay(CallbackInfo callbackInfo) {
        Display.setTitle("Example Client | 1.8.9");
    }
}
```




# Tweaker

Create a class (whatever name you want) and implement ITweaker. Look at the code below.

```java
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* Tweaker Used to Start Mixin Bootstrap - called in Launch Arguments */
public class ExampleTweaker implements ITweaker {

    // List of Launch Arguments for getLaunchArguments[]
    private final List<String> launchArguments = new ArrayList<>();

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        this.launchArguments.addAll(args);

        if (!args.contains("--version") && profile != null) {
            launchArguments.add("--version");
            launchArguments.add(profile);
        }

        if (!args.contains("--assetDir") && assetsDir != null) {
            launchArguments.add("--assetDir");
            launchArguments.add(assetsDir.getAbsolutePath());
        }

        if (!args.contains("--gameDir") && gameDir != null) {
            launchArguments.add("--gameDir");
            launchArguments.add(gameDir.getAbsolutePath());
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        MixinBootstrap.init();

        MixinEnvironment env = MixinEnvironment.getDefaultEnvironment();
        Mixins.addConfiguration("mixins.example.json");

        if (env.getObfuscationContext() == null) {
            env.setObfuscationContext("notch");
        }

        env.setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.toArray(new String[0]);
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

```java
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

Javadocs: https://jenkins.liteloader.com/view/Other/job/Mixin/javadoc/ (The javadocs, pretty self explanatory)

Wiki: https://github.com/SpongePowered/Mixin/wiki (great for learning what Mixins even are)

Discord: https://discord.gg/sponge (Get help with Mixins here)
