// 全局翻译字典
// 全局翻译字典
const genreTranslation = {
    "(no genres listed)": "未分类 (no genres listed)",
    "Action": "动作 (Action)",
    "Adventure": "冒险 (Adventure)",
    "Animation": "动画 (Animation)",
    "Children": "儿童 (Children)",
    "Comedy": "喜剧 (Comedy)",
    "Crime": "犯罪 (Crime)",
    "Documentary": "纪录片 (Documentary)",
    "Drama": "剧情 (Drama)",
    "Fantasy": "奇幻 (Fantasy)",
    "Film-Noir": "黑色电影 (Film-Noir)",
    "Horror": "恐怖 (Horror)",
    "IMAX": "IMAX (IMAX)",
    "Musical": "音乐剧 (Musical)",
    "Mystery": "悬疑 (Mystery)",
    "Romance": "浪漫 (Romance)",
    "Sci-Fi": "科幻 (Sci-Fi)",
    "Thriller": "惊悚 (Thriller)",
    "War": "战争 (War)",
    "Western": "西部 (Western)"
};


function appendMovie2Row(rowId, movieName, movieId, year, rating, rateNumber, genres, baseUrl, type = "genres") {
    var genresOrYearStr = "";

    if (type === "genres") {
        // 如果是按类别，处理类别的显示
        $.each(genres, function (i, genre) {
            const translatedGenre = genreTranslation[genre] || genre;
            genresOrYearStr += ('<div class="genre"><a href="' + baseUrl + 'collection.html?type=genre&value=' + genre + '"><b>' + translatedGenre + '</b></a></div>');
        });
    } else if (type === "years") {
        // 如果是按年份，生成年份的链接
        genresOrYearStr = '<div class="year"><a href="' + baseUrl + 'collection.html?type=year&value=' + year + '"><b>' + year + '</b></a></div>';
    }

    // 生成电影卡片的HTML结构
    var divstr = '<div class="movie-row-item" style="margin-right:5px">\
                    <movie-card-smart>\
                     <movie-card-md1>\
                      <div class="movie-card-md1">\
                       <div class="card">\
                        <link-or-emit>\
                         <a uisref="base.movie" href="./movie.html?movieId=' + movieId + '">\
                         <span>\
                           <div class="poster" style="background-color: #ccc; width: 100%; height: auto; display: flex; align-items: center; justify-content: center;">\
                            <img src="./posters/' + movieId + '.jpg" onerror="this.style.display=\'none\'; this.parentElement.style.backgroundColor=\'#ccc\'; this.parentElement.style.height=\'300px\'; this.parentElement.style.width=\'200px\';" style="width: 100%; height: auto;" />\
                           </div>\
                           </span>\
                           </a>\
                        </link-or-emit>\
                        <div class="overlay">\
                         <div class="above-fold">\
                          <link-or-emit>\
                           <a uisref="base.movie" href="./movie.html?movieId=' + movieId + '">\
                           <span><p class="title">' + movieName + '</p></span></a>\
                          </link-or-emit>\
                          <div class="rating-indicator">\
                           <ml4-rating-or-prediction>\
                            <div class="rating-or-prediction predicted">\
                             <svg xmlns:xlink="http://www.w3.org/1999/xlink" class="star-icon" height="14px" version="1.1" viewbox="0 0 14 14" width="14px" xmlns="http://www.w3.org/2000/svg">\
                              <defs></defs>\
                              <polygon fill-rule="evenodd" points="13.7714286 5.4939887 9.22142857 4.89188383 7.27142857 0.790044361 5.32142857 4.89188383 0.771428571 5.4939887 4.11428571 8.56096041 3.25071429 13.0202996 7.27142857 10.8282616 11.2921429 13.0202996 10.4285714 8.56096041" stroke="none"></polygon>\
                             </svg>\
                             <div class="rating-value">\
                              ' + rating + '\
                             </div>\
                            </div>\
                           </ml4-rating-or-prediction>\
                          </div>\
                         </div>\
                         <div class="below-fold">\
                          <div class="genre-list">\
                           ' + genresOrYearStr + '\
                          </div>\
                          <div class="ratings-display">\
                           <div class="rating-average">\
                            <span class="rating-large">' + rating + '</span>\
                            <span class="rating-total">/5</span>\
                            <p class="rating-caption"> ' + rateNumber + ' 次评分 </p>\
                           </div>\
                          </div>\
                         </div>\
                        </div>\
                       </div>\
                      </div>\
                     </movie-card-md1>\
                    </movie-card-smart>\
                   </div>';

    // 将生成的电影卡片HTML插入到指定的电影行
    $('#' + rowId).append(divstr);
}

function addNavbarLink(navbarId, type, value, translatedValue, baseUrl, isYear = false) {
    const navbarLinks = document.getElementById(navbarId);
    console.log("元素: ", document.getElementById(navbarId));

    const linkItem = document.createElement("li");

    // 设置导航项样式
    linkItem.style.margin = "5px";
    linkItem.style.listStyleType = "none";

    // 根据类别或年份设置背景颜色
    const backgroundColor = type === "year" ? "#b5c0c5" : "#c2e0fa";

    // 动态生成链接
    linkItem.innerHTML = `
    <a href="${baseUrl}collection.html?type=${type}&value=${encodeURIComponent(value)}"
       style="text-decoration: none; color: #fff; padding: 10px 15px; 
              border-radius: 4px; background-color: ${backgroundColor};
              display: block; text-align: center; transition: background-color 0.3s;">
        ${translatedValue}
    </a>`;

    // 添加悬停效果
    let link = linkItem.querySelector("a");
    link.addEventListener("mouseover", function () {
        link.style.backgroundColor = type === "year" ? "#768a77" : "#73639a";
    });
    link.addEventListener("mouseout", function () {
        link.style.backgroundColor = backgroundColor;
    });

    if (isYear) {
        // 检查当前行中是否已有10个年份，创建新的行
        if (!navbarLinks.lastElementChild || navbarLinks.lastElementChild.children.length % 10 === 0) {
            const newRow = document.createElement("ul");
            newRow.style.display = "flex";
            navbarLinks.appendChild(newRow);
        }

        // 将年份项添加到当前行
        navbarLinks.lastElementChild.appendChild(linkItem);
        console.log(navbarLinks.lastElementChild.children.length);
    } else {
        // 否则，直接添加到类别部分
        navbarLinks.appendChild(linkItem);
    }


}



// 在页面中添加电影行的框架（包含标题和滚动区域）
function addRowFrame(pageId, type, rowName, rowId, baseUrl) {
    // 翻译 rowName
    const translatedRowName = genreTranslation[rowName] || rowName;

    // 根据 type 决定跳转的链接
    let linkUrl = '';
    if (type === 'genre') {
        linkUrl = baseUrl + 'collection.html?type=genre&value=' + rowName;
    } else if (type === 'year') {
        linkUrl = baseUrl + 'collection.html?type=year&value=' + rowName;
    }
    console.log("Inside addRowFrame:", pageId, type, rowName, rowId, baseUrl);
    // 创建一个电影行框架的HTML字符串
    var divstr = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 <a class="plainlink" title="go to the full list" href="' + linkUrl + '">' + translatedRowName + '</a> \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId + '" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>';

    // 将电影行框架添加到指定页面的顶部
    $(pageId).prepend(divstr);
}


// 在页面中添加没有链接的电影行框架
function addRowFrameWithoutLink(pageId, rowName, rowId, baseUrl) {
    // 翻译 rowName
    const translatedRowName = genreTranslation[rowName] || rowName;

    var divstr = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 <span class="plainlink">' + translatedRowName + '</span> \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + rowId + '" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>';

    $(pageId).prepend(divstr);
}

function addGenreRow(pageId, rowName, rowId, size, baseUrl) {
    // 翻译 rowName
    const translatedRowName = genreTranslation[rowName] || rowName;

    // 添加电影行框架
    addRowFrame(pageId,"genre", rowName, rowId, baseUrl);

    // 使用AJAX从API获取指定类型的推荐电影
    $.getJSON(baseUrl + "getrecommendation?genre=" + rowName + "&size=" + size + "&sortby=rating", function (result) {
        // 遍历推荐电影数据
        $.each(result, function (i, movie) {
            appendMovie2Row(rowId, movie.title, movie.movieId, movie.releaseYear, movie.averageRating.toPrecision(2), movie.ratingNumber, movie.genres, baseUrl,"genres");
        });
    });
    const curPage = window.location.pathname;
    if(!curPage.includes("collection.html")){
        // 调用封装的函数添加导航链接
        addNavbarLink("navbar-genres","genre", rowName, translatedRowName, baseUrl);
    }

}


// 按年份加载电影
function addYearRow(pageId, rowName, rowId, size, baseUrl) {
    addRowFrame(pageId,"year", rowName, rowId, baseUrl);
    console.log("add Year Row: ", rowName, rowId);
    // 从 API 获取按年份的推荐电影
    $.getJSON(baseUrl + "getrecommendation?year=" + rowName + "&size=" + size + "&sortby=rating")
        .done(function (result) {
            console.log("API request successful:", result);
            $.each(result, function (i, movie) {
                appendMovie2Row(rowId, movie.title, movie.movieId, movie.releaseYear, movie.averageRating.toPrecision(2), movie.ratingNumber, movie.genres, baseUrl, "years");
            });
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            console.log("API request failed:", textStatus, errorThrown);
        });

}

// 添加相关电影行，根据电影ID获取相似电影
function addRelatedMovies(pageId, containerId, movieId, baseUrl) {

    // 创建相关电影区域的HTML框架
    var rowDiv = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 相关电影 \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + containerId + '" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>'
    // 将相关电影区域添加到页面
    $(pageId).prepend(rowDiv);

    // 使用AJAX从API获取相似电影数据
    $.getJSON(baseUrl + "getsimilarmovie?movieId=" + movieId + "&size=16&model=knn", function (result) {
        $.each(result, function (i, movie) {
            // 调用appendMovie2Row函数将相似电影添加到页面
            appendMovie2Row(containerId, movie.title, movie.movieId, movie.releaseYear, movie.averageRating.toPrecision(2), movie.ratingNumber, movie.genres, baseUrl);
        });
    });
}

// 添加用户历史记录行，根据用户ID获取观看历史
function addUserHistory(pageId, containerId, userId, baseUrl) {
    // 修改HTML结构，添加折叠按钮
    var rowDiv = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 观影记录 \
                 <button id="toggleHistory" class="btn btn-link" style="float: right; padding: 0 10px;">展示全部</button>\
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + containerId + '" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>';

    // 将用户历史记录区域添加到页面
    $(pageId).prepend(rowDiv);

    // 存储所有电影数据的数组
    let allMovies = [];
    let isExpanded = false;

    // 从API获取用户历史记录数据
    $.getJSON(baseUrl + "getuser?id=" + userId, function (userObject) {
        // 创建一个计数器来追踪已加载的电影数量
        let loadedCount = 0;
        const totalRatings = userObject.ratings.length;

        // 使用Promise.all来等待所有电影数据加载完成
        Promise.all(userObject.ratings.map(rating => {
            return new Promise((resolve) => {
                $.getJSON(baseUrl + "getmovie?id=" + rating.rating.movieId, function (movieObject) {
                    allMovies.push({
                        title: movieObject.title,
                        movieId: movieObject.movieId,
                        releaseYear: movieObject.releaseYear,
                        score: rating.rating.score,
                        ratingNumber: movieObject.ratingNumber,
                        genres: movieObject.genres
                    });
                    resolve();
                });
            });
        })).then(() => {
            // 所有数据加载完成后，先显示前32部电影
            displayMovies(false);

            // 添加折叠按钮点击事件
            $('#toggleHistory').click(function () {
                isExpanded = !isExpanded;
                $(this).text(isExpanded ? '展示部分' : '展示全部');
                displayMovies(isExpanded);
            });
        });
    });

    // 显示电影的函数
    function displayMovies(showAll) {
        // 清空容器
        $(`#${containerId}`).empty();

        // 确定要显示的电影数量
        const displayCount = showAll ? allMovies.length : Math.min(32, allMovies.length);

        // 显示电影
        for (let i = 0; i < displayCount; i++) {
            const movie = allMovies[i];
            appendMovie2Row(
                containerId,
                movie.title,
                movie.movieId,
                movie.releaseYear,
                movie.score,
                movie.ratingNumber,
                movie.genres,
                baseUrl
            );
        }
    }
}

// 添加为用户推荐的电影行，根据用户ID和推荐模型获取推荐电影
function addRecForYou(pageId, containerId, userId, model, baseUrl) {

    var rowDiv = '<div class="frontpage-section-top"> \
                <div class="explore-header frontpage-section-header">\
                 为你推荐 \
                </div>\
                <div class="movie-row">\
                 <div class="movie-row-bounds">\
                  <div class="movie-row-scrollable" id="' + containerId + '" style="margin-left: 0px;">\
                  </div>\
                 </div>\
                 <div class="clearfix"></div>\
                </div>\
               </div>'
    // 将推荐电影区域添加到页面
    $(pageId).prepend(rowDiv);

    // 使用AJAX从API获取为用户推荐的电影数据
    $.getJSON(baseUrl + "getrecforyou?id=" + userId + "&size=32&model=" + model, function (result) {
        $.each(result, function (i, movie) {
            // 调用appendMovie2Row函数将推荐的电影添加到页面
            appendMovie2Row(containerId, movie.title, movie.movieId, movie.releaseYear, movie.averageRating.toPrecision(2), movie.ratingNumber, movie.genres, baseUrl);
        });
    });
}

// 添加电影详情到指定容器
function addMovieDetails(containerId, movieId, baseUrl) {
    $.getJSON(baseUrl + "getmovie?id=" + movieId, function (movieObject) {
        var genres = "";
        $.each(movieObject.genres, function (i, genre) {
            const translatedGenre = genreTranslation[genre] || genre;
            genres += ('<span><a href="' + baseUrl + 'collection.html?type=genre&value=' + genre + '"><b>' + translatedGenre + '</b></a>');
            if (i < movieObject.genres.length - 1) {
                genres += ", </span>";
            } else {
                genres += "</span>";
            }
        });

        var ratingUsers = "";
        $.each(movieObject.topRatings, function (i, rating) {
            ratingUsers += ('<span><a href="' + baseUrl + 'user.html?id=' + rating.rating.userId + '"><b>User' + rating.rating.userId + '</b></a>');
            if (i < movieObject.topRatings.length - 1) {
                ratingUsers += ", </span>";
            } else {
                ratingUsers += "</span>";
            }
        });

        var movieDetails = '<div class="row movie-details-header movie-details-block">\
                                <div class="col-md-2 header-backdrop">\
                                    <img alt="movie backdrop image" height="250" src="./posters/' + movieObject.movieId + '.jpg">\
                                </div>\
                                <div class="col-md-9"><h1 class="movie-title"> ' + movieObject.title + ' </h1>\
                                    <div class="row movie-highlights">\
                                        <div class="col-md-2">\
                                            <div class="heading-and-data">\
                                                <div class="movie-details-heading">链接</div>\
                                                <a target="_blank" href="http://www.imdb.com/title/tt' + movieObject.imdbId + '">imdb</a>,\
                                                <span><a target="_blank" href="http://www.themoviedb.org/movie/' + movieObject.tmdbId + '">tmdb</a></span>\
                                            </div>\
                                        </div>\
                                        <div class="col-md-6">\
                                            <div class="heading-and-data">\
                                                <div class="movie-details-heading">类型</div>\
                                                ' + genres + '\
                                            </div>\
                                            <div class="heading-and-data">\
                                                <div class="movie-details-heading">谁最喜欢这部电影</div>\
                                                ' + ratingUsers + '\
                                            </div>\
                                        </div>\
                                    </div>\
                                </div>\
                            </div>';
        $("#" + containerId).prepend(movieDetails);
    });
}


// 添加用户详细信息到指定容器
function addUserDetails(containerId, userId, baseUrl) {

    // 使用AJAX从API获取用户的详细信息
    $.getJSON(baseUrl + "getuser?id=" + userId, function (userObject) {
        var userDetails = '<div class="row movie-details-header movie-details-block">\
                                        <div class="col-md-2 header-backdrop">\
                                            <img alt="movie backdrop image" height="200" src="./images/avatar/'+ userObject.userId % 10 + '.png">\
                                        </div>\
                                        <div class="col-md-9"><h1 class="movie-title"> 用户'+ userObject.userId + ' </h1>\
                                            <div class="row movie-highlights">\
                                                <div class="col-md-2">\
                                                    <div class="heading-and-data">\
                                                        <div class="movie-details-heading"> 已观影数量 </div>\
                                                        <div> '+ userObject.ratingCount + ' </div>\
                                                    </div>\
                                                    <div class="heading-and-data">\
                                                        <div class="movie-details-heading"> 用户平均评分 </div>\
                                                        <div> '+ userObject.averageRating.toPrecision(2) + ' 星\
                                                        </div>\
                                                    </div>\
                                                </div>\
                                                <div class="col-md-3">\
                                                    <div class="heading-and-data">\
                                                        <div class="movie-details-heading"> 用户最高评分 </div>\
                                                        <div> '+ userObject.highestRating + ' 星</div>\
                                                    </div>\
                                                    <div class="heading-and-data">\
                                                        <div class="movie-details-heading"> 用户最低评分 </div>\
                                                        <div> '+ userObject.lowestRating + ' 星\
                                                        </div>\
                                                    </div>\
                                                </div>\
                                            </div>\
                                        </div>\
                                    </div>'
        // 将用户详情添加到指定容器
        $("#" + containerId).prepend(userDetails);
    });
};

// 下面代码是处理搜索框提交表单的

// 处理表单提交的搜索逻辑
function handleSearch(event) {
    event.preventDefault(); // 阻止表单默认提交行为
    const query = document.getElementById("omnisearch-typeahead").value.trim(); // 获取搜索输入
    if (!query) {
        alert("Please enter a search term.");
        return false;
    }

    // 如果在 search.html 页面，直接发送请求并显示结果；否则跳转到 search.html
    if (window.location.pathname.includes("search.html")) {
        // 在 search.html 页面执行搜索
        fetch(`/search?query=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(data => {
                displaySearchResults(data); // 渲染搜索结果
            })
            .catch(error => {
                console.error("Error during search:", error);
            });
    } else {
        // 在 index.html 页面跳转到 search.html 页面
        window.location.href = `/search.html?query=${encodeURIComponent(query)}`;
    }
    return false;
}

// 显示搜索建议
function suggestSearch(event) {
    const query = event.target.value.trim();
    const suggestionsBox = document.getElementById("search-suggestions");

    if (!query) {
        suggestionsBox.style.display = "none"; // 隐藏建议框
        return;
    }

    // 模拟建议数据的获取（实际应通过后端接口获取）
    const mockSuggestions = ["Action", "Comedy", "Romance", "Thriller", "Drama"].filter(item => item.toLowerCase().includes(query.toLowerCase()));

    if (mockSuggestions.length > 0) {
        suggestionsBox.innerHTML = mockSuggestions
            .map(item => `<div onclick="selectSuggestion('${item}')">${item}</div>`)
            .join("");
        suggestionsBox.style.display = "block"; // 显示建议框
    } else {
        suggestionsBox.style.display = "none"; // 隐藏建议框
    }
}

// 用户点击建议时填充到输入框
function selectSuggestion(suggestion) {
    document.getElementById("omnisearch-typeahead").value = suggestion;
    document.getElementById("search-suggestions").style.display = "none"; // 隐藏建议框
}

// 渲染搜索结果
function displaySearchResults(results) {
    const container = document.getElementById("search-results");
    const queryContainer = document.getElementById("search-query");

    // 清空容器内容
    container.innerHTML = "";
    queryContainer.innerHTML = "";

    if (!Array.isArray(results)) {
        console.error("Expected an array of results, but got:", results);
        container.innerHTML = "<p>Error: Results data is not in expected format.</p>";
        return;
    }

    // 显示搜索的关键词
    const query = new URLSearchParams(window.location.search).get('query');
    queryContainer.innerHTML = `<p>Showing results for: <strong>${decodeURIComponent(query)}</strong></p>`;

    if (results.length === 0) {
        container.innerHTML = "<p>No results found.</p>";
        return;
    }
    var getUrl = window.location;
    var baseUrl = getUrl.protocol + "//" + getUrl.host + "/"
    console.log(baseUrl)
    // 遍历搜索结果并渲染
    results.forEach(movie => {
        // 如果 genres 是一个字符串，先转换为数组
        let genresArray = movie.genres.slice(1, -1).split(',').map(item => item.trim());
        console.log("movieId:", movie.movieId); // 确保它是有效的

        appendMovie2Row(
            'search-results',                // 目标容器的ID
            movie.title,                      // 电影名称
            movie.movieId,                    // 电影ID
            movie.year,                       // 电影年份
            movie.rating,                     // 电影评分
            movie.rateNumber,                 // 评分数量
            genresArray,                      // 电影类别（数组）
            baseUrl     // 基础URL
        );
    });
}

