const async = require('async')
const { fetchUrl } = require("fetch")
const http = require('http')

const getQueryParam = (url, key, defaultValue) => new URL(`http://localhost${url}`).searchParams.get(key) || defaultValue

const remote = process.env.REMOTE_ADDR || 'http://localhost:8000/worker'

console.log(`remote server ${remote}`)

const server = http.createServer((req, res) => {
    console.log(req.url)

    if (/^\/manager(.*)?/.test(req.url)) {
        const repeat = getQueryParam(req.url, 'n', 100)

        async.mapLimit(new Array(repeat), repeat, (_, cb) => {
            fetchUrl(remote, (e, _, body) => {
                if (e) {
                    cb(e, null)
                } else {
                    cb(null, body.toString())
                }
            })
        }, (e, result) => {
            if (e) {
                console.log(e)
                res.writeHead(500, {"Content-Type": "text/plain"})
                res.write(e.message)
                res.end()
            } else {
                res.writeHead(200, {'Content-Type': 'application/json'})
                res.write(JSON.stringify(result));
                res.end()
            }
        })
    } else {
        res.writeHead(404, {"Content-Type": "text/plain"})
        res.write("404 Not Found")
        res.end()
    }
})

server.listen(8080)

console.log('server is running at port 8080')
