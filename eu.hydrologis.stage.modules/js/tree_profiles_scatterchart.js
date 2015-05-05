function treeProfileScatterChart() {
    var _chart = {};
    var _width = 600, _height = 300, // <-1B
            _margins = {top: 30, left: 60, right: 20, bottom: 20},
    _x,
            _y,
            _p,
            _h,
            _pm,
            _hm,
            _data = [],
            _color = "red",
            _dotsize = 2,
            _svg,
            _bodyG;

    _chart.render = function () {
        if (!_svg) {
            _svg = d3.select("body").append("svg") // <-2B
                    .attr("height", _height)
                    .attr("width", _width);

            renderAxes(_svg);

            defineBodyClip(_svg);
        }

        renderBody(_svg);
    };

    _chart.render = function (svgAppendId) {
        if (!_svg) {
            _svg = d3.select("#" + svgAppendId).append("svg") // <-2B
                    .attr("height", _height)
                    .attr("width", _width);

            renderAxes(_svg);

            defineBodyClip(_svg);
        }

        renderBody(_svg);
    };

    function renderAxes(svg) {
        var axesG = svg.append("g")
                .attr("class", "axes");
        renderXAxis(axesG);
        renderYAxis(axesG);
    }

    function renderXAxis(axesG) {
        var xAxis = d3.svg.axis()
                .scale(_x.range([0, quadrantWidth()]))
                .orient("bottom");
        axesG.append("g")
                .attr("class", "x axis")
                .attr("transform", function () {
                    return "translate(" + xStart() + "," + yStart() + ")";
                })
                .call(xAxis);

        d3.selectAll("g.x g.tick")
                .append("line")
                .classed("grid-line", true)
                .attr("x1", 0)
                .attr("y1", 0)
                .attr("x2", 0)
                .attr("y2", -quadrantHeight());
    }

    function renderYAxis(axesG) {
        var yAxis = d3.svg.axis()
                .scale(_y.range([quadrantHeight(), 0]))
                .orient("left");

        axesG.append("g")
                .attr("class", "y axis")
                .attr("transform", function () {
                    return "translate(" + xStart() + "," + yEnd() + ")";
                })
                .call(yAxis);

        d3.selectAll("g.y g.tick")
                .append("line")
                .classed("grid-line", true)
                .attr("x1", 0)
                .attr("y1", 0)
                .attr("x2", quadrantWidth())
                .attr("y2", 0);
    }


    function defineBodyClip(svg) { // <-2C
        var padding = 5;
        svg.append("defs")
                .append("clipPath")
                .attr("id", "body-clip")
                .append("rect")
                .attr("x", 0 - padding)
                .attr("y", 0)
                .attr("width", quadrantWidth() + 2 * padding)
                .attr("height", quadrantHeight());
    }

    function renderBody(svg) { // <-2D
        if (!_bodyG)
            _bodyG = svg.append("g")
                    .attr("class", "body")
                    .attr("transform", "translate("
                            + xStart() + ","
                            + yEnd() + ")") // <-2E
                    .attr("clip-path", "url(#body-clip)");
        renderDots();
    }

    function renderDots() {
        _bodyG.selectAll("circle")
                .data(_data)
                .enter()
                .append("circle")
                .style("fill", _color)
                .attr("cx", function (d) {
                    return _x(d.p);
                })
                .attr("cy", function (d) {
                    return _y(d.h);
                })
                .attr("r", _dotsize + "px");

        if (_p) {
            _bodyG.append("line")
                    .style("stroke", "black")
                    .style("stroke-width", 4)
                    .attr("x1", _x(_p))
                    .attr("y1", _y(0))
                    .attr("x2", _x(_p))
                    .attr("y2", _y(_h));
            _bodyG.append("line")
                    .style("stroke", "yellow")
                    .style("stroke-width", 2)
                    .attr("x1", _x(_p))
                    .attr("y1", _y(0))
                    .attr("x2", _x(_p))
                    .attr("y2", _y(_h));
        }
        if (_pm) {
            _bodyG.append("line")
                    .style("stroke", "black")
                    .style("stroke-width", 4)
                    .attr("x1", _x(_pm))
                    .attr("y1", _y(0))
                    .attr("x2", _x(_pm))
                    .attr("y2", _y(_hm));
            _bodyG.append("line")
                    .style("stroke", "grey")
                    .style("stroke-width", 2)
                    .attr("x1", _x(_pm))
                    .attr("y1", _y(0))
                    .attr("x2", _x(_pm))
                    .attr("y2", _y(_hm));
        }
    }

    function xStart() {
        return _margins.left;
    }
    function yStart() {
        return _height - _margins.bottom;
    }
    function xEnd() {
        return _width - _margins.right;
    }
    function yEnd() {
        return _margins.top;
    }
    function quadrantWidth() {
        return _width - _margins.left - _margins.right;
    }
    function quadrantHeight() {
        return _height - _margins.top - _margins.bottom;
    }

    _chart.width = function (w) {
        if (!arguments.length)
            return _width;
        _width = w;
        return _chart;
    };

    _chart.height = function (h) { // <-1C
        if (!arguments.length)
            return _height;
        _height = h;
        return _chart;
    };

    _chart.margins = function (m) {
        if (!arguments.length)
            return _margins;
        _margins = m;
        return _chart;
    };

    _chart.color = function (c) {
        if (!arguments.length)
            return _color;
        _color = c;
        return _chart;
    };

    _chart.dotsize = function (c) {
        if (!arguments.length)
            return _dotsize;
        _dotsize = c;
        return _chart;
    };

    _chart.x = function (x) {
        if (!arguments.length)
            return _x;
        _x = x;
        return _chart;
    };

    _chart.y = function (y) {
        if (!arguments.length)
            return _y;
        _y = y;
        return _chart;
    };

    _chart.centerTreeProg = function (p, h) {
        if (!arguments.length)
            return [_p, _h];
        _p = p;
        _h = h;
        return _chart;
    };

    _chart.centerMatchedTreeProg = function (p, h) {
        if (!arguments.length)
            return [_pm, _hm];
        _pm = p;
        _hm = h;
        return _chart;
    };

    _chart.setData = function (data) { // <-1D
        _data = data;
        return _chart;
    };
    return _chart; // <-1E
}
