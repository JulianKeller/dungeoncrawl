### Setting Up JIG with IntelliJ

This guide explains how to setup a directory where multiple slick games can be developed with `JIG` from within Intellij. Once this setup is complete you can create as many `JIG` games in the `slickgames` directory (covered below) without having to re-setup Intellij for use with `JIG`.

## Basic Process

1. Create a folder to hold all of your game projects. I'll call mine `slickgames`

2. `cd slickgames` . 

3. Clone the JIG repo here as well `git clone https://gitlab.encs.vancouver.wsu.edu/wallaces/JIG.git`

4. Verify your directory structure. Inside `slickgames` you should see a `JIG` folder.

5. - Start IntelliJ. Select `New Project`. 
    - Select `Java` and set your Java version under the `Project SDK` dropdown. 
    - Click Next and Next again.
    - Set `Project location` to `slickgames`; click on the `more settings` fold out and change the module name to `my-game`. 
    - The `Content root` and `Module file location` should now be set to `.../slickgames/my-game`.
    - Click Next.
    - Intellij might warn and ask if you want to overwrite the folder as it isn't empty. Select `yes`.

6. The project now has `my-game`, but not JIG... so

7.  - Open  Project Structure (`file>Project Structure`) and navigate to the Modules tab under `Project Settings`. 
    - Click the `+` to and select `Add Module`.
    - Be sure to set the Module SDK to the same Java version as the Project SDK and click `Next`.
    - Set `Module Name` to `JIG` and ensure the `Module location` and `Content Root`  is set to `slickgames/JIG`.

8. - Now you should see all the resources available in your project, but IntelliJ is confused by the layout. 
    - Delete `slickgames/JIG/src` 
    - Right click   `slickgames/JIG/jig/src` and select `Mark Directory as>Sources Root` 

9. - Again in Project Structure" (`file>Project Structure`) , under `Project Settings>Libraries tab`, 
    - Click the `+`  and select `Java`. 
    - Select 
	- `JIG/lib/slick.jar`
	- `JIG/lib/slick-sources.jar`
    - Click Ok
    - Shift + Click and select both `JIG` and `my-game` and click OK.

10.  - Again in Project Structure" (`file>Project Structure`) , under `Project Settings>Libraries tab`, 
        - Select all files in `JIG/lib/lwjgl-2.9.3/jar` *except* for 
            - `lwjgl-debug.jar`, 
            - `lwjgl_test.jar` and 
            - `lwjgl_util_applet.jar`. 
            and again add to both modules as in Step 9.
        - Click OK.

11. You should be able to build the code in `JIG/jig/src` now. Right click `JIG/` and click `Build Module JIG`.

12.   - Again in Project Structure" (`file>Project Structure`) , under `Project Settings>Modules tab`,  select the `my-game` module.
        - Under the `Dependencies` tab click the `+`  select `Module Dependency...` and add `JIG`
        - Under the `Dependencies` tab click the `+`  select `Jar or Directories...` and add  `JIG/lib/lwjgl-2.9.3/native/<your os>` folder.
        - IntelliJ may be unsure what these are and ask you -- select "Native libraries" or just display a red `Empty Library` folder, this is ok.
        - Click OK

13. Now you should be able to run `my-game/<my-game-main>.java`
