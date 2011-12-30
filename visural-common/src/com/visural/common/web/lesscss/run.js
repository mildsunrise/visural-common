var lessIt = function(data, input, includes, compress, yuicompress, optlevel) {
    var result;
    var error;
    
    new(less.Parser)({
        paths: includes,
        optimization: optlevel,
        filename: input
    }).parse(data, function (err, tree) {
        error = err;
        try {
            css = tree.toCSS({
                compress: compress,
                yuicompress: yuicompress
            });
            result = css;
        } catch (e) {
            error = err;
        }
    });
    
    if (error) {
        throw error;
    }
    return result;
};
