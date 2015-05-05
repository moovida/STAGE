
function maximumDocumentSizes(margins) {
    var instance = {};
    var width = 500, height = 500;
    if (document.body && document.body.offsetWidth) {
        width = document.body.offsetWidth;
        height = document.body.offsetHeight;
    }
    if (document.compatMode === 'CSS1Compat' &&
            document.documentElement &&
            document.documentElement.offsetWidth) {
        width = document.documentElement.offsetWidth;
        height = document.documentElement.offsetHeight;
    }
    if (window.innerWidth && window.innerHeight) {
        width = window.innerWidth;
        height = window.innerHeight;
    }
    instance.height = height - margins.top - margins.bottom;
    instance.width = width - margins.left - margins.right;
    return instance;
}


