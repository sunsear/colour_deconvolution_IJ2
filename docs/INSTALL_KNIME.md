## Installing this plugin into FIJI

After [downloading the plugin from maven](DOWNLOAD_PLUGIN.md), open up Knime. 

### Adding the experimental update site
* Open the Knime Preferences.
* Navigate to Install/Update
* Open Available Software Sites
* Enable Knime Community Extensions (Experimental)
 
![Adding the experimental extensions](img/knime_add_update_site.png)

### Adding the ImageJ extension to Knime
* Open the File menu
* Open "Install knime extensions"

![Menu location for installing knime extensions](img/knime_menu_install_extensions.png)

* Search for Image and install both
  * Knime Image Processing and
  * Knime Image Processing - ImageJ Integration (Beta)

![Choosing the right extensions](img/knime_install_imagej.png)

* Restart Knime

### Adding the Colour Deconvolution IJ2 to Knime
* Open Preferences
* Navigate to:
  * Knime
  * Image Processing Plugin
  * ImageJ2 Plugin Install
  
![Adding the colour deconvolution plugin](img/knime_add_colour_deconv.png)

* Click on the add button
* Choose the jar file you downloaded earlier
* Restart Knime

### Success
* You should now have the Colour Deconvolution block available in the Node repository of knime:

![The colour deconvolution block in the node repo](img/knime_success.png) 

* Enjoy!

### Want to hit the ground running? 
Try our Knime workflow that incorporates the plugin to do analysis of a user defined selection of images: 

https://hub.knime.com/boudewijn/spaces/Public/latest/HistogramOfDeconvolutedImagesWithOriginal