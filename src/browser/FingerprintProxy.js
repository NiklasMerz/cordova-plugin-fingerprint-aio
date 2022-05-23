var Fingerprint = function () {};

/* Plugin Errors */
Fingerprint.prototype.BIOMETRIC_UNKNOWN_ERROR = -100;
Fingerprint.prototype.BIOMETRIC_UNAVAILABLE = -101;
Fingerprint.prototype.BIOMETRIC_AUTHENTICATION_FAILED = -102;
Fingerprint.prototype.BIOMETRIC_SDK_NOT_SUPPORTED = -103;
Fingerprint.prototype.BIOMETRIC_HARDWARE_NOT_SUPPORTED = -104;
Fingerprint.prototype.BIOMETRIC_PERMISSION_NOT_GRANTED = -105;
Fingerprint.prototype.BIOMETRIC_NOT_ENROLLED = -106;
Fingerprint.prototype.BIOMETRIC_INTERNAL_PLUGIN_ERROR = -107;
Fingerprint.prototype.BIOMETRIC_DISMISSED = -108;
Fingerprint.prototype.BIOMETRIC_PIN_OR_PATTERN_DISMISSED = -109;
Fingerprint.prototype.BIOMETRIC_SCREEN_GUARD_UNSECURED = -110;
Fingerprint.prototype.BIOMETRIC_LOCKED_OUT = -111;
Fingerprint.prototype.BIOMETRIC_LOCKED_OUT_PERMANENT = -112;
Fingerprint.prototype.BIOMETRIC_NO_SECRET_FOUND = -113;

/* Plugin return values */
Fingerprint.prototype.STR_BIOMETRIC_AUTHENTICATION_FAILED = "BIOMETRIC_AUTHENTICATION_FAILED";
Fingerprint.prototype.STR_BIOMETRIC_DISMISSED = "BIOMETRIC_DISMISSED";
Fingerprint.prototype.STR_BIOMETRIC_SUCCESS = "biometric_success";

/* Biometric types */
Fingerprint.prototype.BIOMETRIC_TYPE_FINGERPRINT = "finger";
Fingerprint.prototype.BIOMETRIC_TYPE_FACE = "face";
Fingerprint.prototype.BIOMETRIC_TYPE_COMMON = "biometric";

/* Browser features */
Fingerprint.prototype.HIGHEST_POSSIBLE_Z_INDEX = 2147483647;
Fingerprint.prototype.FINGERPRINT_BG_DIV = 'cordova_fingerprint_plugin_bg';
Fingerprint.prototype.FINGERPRINT_FG_DIV = 'cordova_fingerprint_plugin_fg';

/* Browser buttons */
Fingerprint.prototype.ICON_SUCCESS_SRC = 'fingerprint-success.png';
Fingerprint.prototype.ICON_SUCCESS_ID = 'cordova_plugin_fingerprint_icon_success';
Fingerprint.prototype.ICON_DISMISS_SRC = 'fingerprint-chevron-left.png';
Fingerprint.prototype.ICON_DISMISS_ID = 'cordova_plugin_fingerprint_icon_dismiss';
Fingerprint.prototype.ICON_FAILED_SRC = 'fingerprint-close.png';
Fingerprint.prototype.ICON_FAILED_ID = 'cordova_plugin_fingerprint_icon_failed';
Fingerprint.prototype.ICON_SIZE = '';

Fingerprint.prototype.authenticate = function (successCallback, errorCallback, params) {
	/* translucid background div */
	var dialogBg = document.createElement('div');
	dialogBg.id = Fingerprint.prototype.FINGERPRINT_BG_DIV;
	dialogBg.style.backgroundColor = '#00000088';
	dialogBg.style.position = 'fixed';
	dialogBg.style.top = '0';
	dialogBg.style.bottom = '0';
	dialogBg.style.left = '0';
	dialogBg.style.right = '0';
	dialogBg.style.zIndex = (Fingerprint.prototype.HIGHEST_POSSIBLE_Z_INDEX - 1);
	document.body.appendChild(dialogBg);

	/* foreground text div */
	var dialogFg = document.createElement('div');
	dialogFg.id = Fingerprint.prototype.FINGERPRINT_FG_DIV;
	dialogFg.setAttribute('style', 'transform: translateY(-50%);');
	dialogFg.style.backgroundColor = '#424242';
	dialogFg.style.boxShadow = 'rgba(0,0,0,0.5) 0px 4px 24px';
	dialogFg.style.borderRadius = '3px';
	dialogFg.style.marginLeft = 'auto';
	dialogFg.style.marginRight = 'auto';
	dialogFg.style.padding = '1.5em';
	dialogFg.style.position = 'relative';
	dialogFg.style.top = '50%';
	dialogFg.style.textAlign = 'center';
	dialogFg.style.width = '70%';
	dialogFg.style.zIndex = Fingerprint.prototype.HIGHEST_POSSIBLE_Z_INDEX;

	/* title */
	var title = '';
	if (params && typeof (params[0]) === 'object' && typeof (params[0].title) === 'string' && params[0].title.length > 0) {
		title = params[0].title;
	} else {
		title = 'Biometric Sign On';
	}
	var divTitle = document.createElement('div');
	divTitle.appendChild(document.createTextNode(title));
	divTitle.style.color = '#FFFFFF';
	divTitle.style.fontSize = '20px';
	divTitle.style.fontWeight = '600';
	divTitle.style.lineHeight = '2em';
	divTitle.style.textAlign = 'center';
	dialogFg.appendChild(divTitle);

	/* optional subtitle */
	if (params && typeof (params[0]) === 'object' && typeof (params[0].subtitle) === 'string' && params[0].subtitle.length > 0) {
		var divSubtitle = document.createElement('div');
		divSubtitle.appendChild(document.createTextNode(params[0].subtitle));
		divSubtitle.style.color = '#BDBDBD';
		divSubtitle.style.fontSize = '14px';
		divSubtitle.style.lineHeight = '2em';
		divSubtitle.style.textAlign = 'center';
		dialogFg.appendChild(divSubtitle);
	}

	/* optional description */
	if (params && typeof (params[0]) === 'object' && typeof (params[0].description) === 'string' && params[0].description.length > 0) {
		var divDescription = document.createElement('div');
		divDescription.appendChild(document.createTextNode(params[0].description));
		divDescription.style.color = '#BDBDBD';
		divDescription.style.fontSize = '14px';
		divDescription.style.lineHeight = '2em';
		divDescription.style.textAlign = 'center';
		dialogFg.appendChild(divDescription);
	}

	/* BIOMETRIC_DISMISSED */
	var imgIconDismissed = document.createElement('img');
	imgIconDismissed.id = Fingerprint.prototype.ICON_DISMISS_ID;
	imgIconDismissed.src = Fingerprint.prototype.ICON_DISMISS_SRC;
	imgIconDismissed.style.backgroundColor = '#FFFFFF10';
	imgIconDismissed.style.borderColor = '#FFFFFF30';
	imgIconDismissed.style.borderRadius = '50%';
	imgIconDismissed.style.borderStyle = 'dashed';
	imgIconDismissed.style.borderWidth = '2px';
	imgIconDismissed.style.cursor = 'pointer';
	imgIconDismissed.style.display = 'inline-block';
	imgIconDismissed.style.margin = '8px';
	imgIconDismissed.style.marginTop = '24px';
	imgIconDismissed.style.padding = '8px';
	imgIconDismissed.style.minWidth = '32px';
	imgIconDismissed.style.maxWidth = '96px';
	imgIconDismissed.style.width = '20%';
	imgIconDismissed.title = Fingerprint.prototype.STR_BIOMETRIC_DISMISSED;
	imgIconDismissed.addEventListener('mouseover', function () {
		imgIconDismissed.style.backgroundColor = '#FFFFFF20';
	});
	imgIconDismissed.addEventListener('mouseout', function () {
		imgIconDismissed.style.backgroundColor = '#FFFFFF10';
	});
	imgIconDismissed.addEventListener('click', function () {
		document.getElementById(Fingerprint.prototype.FINGERPRINT_BG_DIV).remove();
		errorCallback({code: Fingerprint.prototype.BIOMETRIC_DISMISSED, message: Fingerprint.prototype.STR_BIOMETRIC_DISMISSED});
	});
	dialogFg.appendChild(imgIconDismissed);

	/* BIOMETRIC_SUCCESS */
	var imgIconSuccess = document.createElement('img');
	imgIconSuccess.id = Fingerprint.prototype.ICON_SUCCESS_ID;
	imgIconSuccess.src = Fingerprint.prototype.ICON_SUCCESS_SRC;
	imgIconSuccess.style.backgroundColor = '#FFFFFF10';
	imgIconSuccess.style.borderColor = '#FFFFFF30';
	imgIconSuccess.style.borderRadius = '50%';
	imgIconSuccess.style.borderStyle = 'dashed';
	imgIconSuccess.style.borderWidth = '2px';
	imgIconSuccess.style.cursor = 'pointer';
	imgIconSuccess.style.display = 'inline-block';
	imgIconSuccess.style.margin = '8px';
	imgIconSuccess.style.marginTop = '24px';
	imgIconSuccess.style.padding = '8px';
	imgIconSuccess.style.minWidth = '32px';
	imgIconSuccess.style.maxWidth = '96px';
	imgIconSuccess.style.width = '20%';
	imgIconSuccess.title = Fingerprint.prototype.STR_BIOMETRIC_SUCCESS;
	imgIconSuccess.addEventListener('mouseover', function () {
		imgIconSuccess.style.backgroundColor = '#FFFFFF20';
	});
	imgIconSuccess.addEventListener('mouseout', function () {
		imgIconSuccess.style.backgroundColor = '#FFFFFF10';
	});
	imgIconSuccess.addEventListener('click', function () {
		document.getElementById(Fingerprint.prototype.FINGERPRINT_BG_DIV).remove();
		successCallback(Fingerprint.prototype.STR_BIOMETRIC_SUCCESS);
	});
	dialogFg.appendChild(imgIconSuccess);

	/* BIOMETRIC_AUTHENTICATION_FAILED */
	var imgIconFailed = document.createElement('img');
	imgIconFailed.id = Fingerprint.prototype.ICON_FAILED_ID;
	imgIconFailed.src = Fingerprint.prototype.ICON_FAILED_SRC;
	imgIconFailed.style.backgroundColor = '#FFFFFF10';
	imgIconFailed.style.borderColor = '#FFFFFF30';
	imgIconFailed.style.borderRadius = '50%';
	imgIconFailed.style.borderStyle = 'dashed';
	imgIconFailed.style.borderWidth = '2px';
	imgIconFailed.style.cursor = 'pointer';
	imgIconFailed.style.display = 'inline-block';
	imgIconFailed.style.margin = '8px';
	imgIconFailed.style.marginTop = '24px';
	imgIconFailed.style.padding = '8px';
	imgIconFailed.style.minWidth = '32px';
	imgIconFailed.style.maxWidth = '96px';
	imgIconFailed.style.width = '20%';
	imgIconFailed.title = Fingerprint.prototype.STR_BIOMETRIC_AUTHENTICATION_FAILED;
	imgIconFailed.addEventListener('mouseover', function () {
		imgIconFailed.style.backgroundColor = '#FFFFFF20';
	});
	imgIconFailed.addEventListener('mouseout', function () {
		imgIconFailed.style.backgroundColor = '#FFFFFF10';
	});
	imgIconFailed.addEventListener('click', function () {
		document.getElementById(Fingerprint.prototype.FINGERPRINT_BG_DIV).remove();
		errorCallback({code: Fingerprint.prototype.BIOMETRIC_AUTHENTICATION_FAILED, message: Fingerprint.prototype.STR_BIOMETRIC_AUTHENTICATION_FAILED});
	});
	dialogFg.appendChild(imgIconFailed);

	document.getElementById(Fingerprint.prototype.FINGERPRINT_BG_DIV).appendChild(dialogFg);
};

Fingerprint.prototype.isAvailable = function (successCallback, errorCallback, optionalParams) {
	successCallback(Fingerprint.prototype.BIOMETRIC_TYPE_COMMON);
};

Fingerprint.prototype.registerBiometricSecret = function (params, successCallback, errorCallback) {
	cordova.exec(
			  successCallback,
			  errorCallback,
			  "Fingerprint",
			  "registerBiometricSecret",
			  [params]
			  );
};

Fingerprint.prototype.loadBiometricSecret = function (params, successCallback, errorCallback) {
	cordova.exec(
			  successCallback,
			  errorCallback,
			  "Fingerprint",
			  "loadBiometricSecret",
			  [params]
			  );
};

module.exports = new Fingerprint();

require('cordova/exec/proxy').add('Fingerprint', module.exports);