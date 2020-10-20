package main

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
)

func ping(c *gin.Context) {
	c.JSON(200, gin.H{
		"message": "pong",
	})
}

func worker(c *gin.Context) {
	time.Sleep(5*time.Second)
	c.JSON(200, gin.H{
		"message": "pong",
	})
}

const remoteURL = "http://worker:8000/worker"

func download(idx int, c chan string){
	resp, err := http.Get(remoteURL)
	if err != nil{
		fmt.Println("download error: ", err)
		c <- err.Error()
	}
	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)
	if err != nil{
		fmt.Println("ioread error ", err)
		c <- err.Error()
	}
	c <- string(body)
}

func manager(c *gin.Context){
	ch := make(chan string, 1)
	var loops int = 100
	for i:=0;i<loops;i++{
		go download(i, ch)
	}
	var arr []string
	for i:=0;i<loops;i++{
		s := <-ch
		arr = append(arr, s)
	}
	c.JSON(200, gin.H{
		"msg": "ok",
		"res": arr,
		"num": len(arr),
	})
}

func main() {
	r := gin.Default()
	r.GET("/ping", ping)
	r.GET("/worker", worker)
	r.GET("/manager", manager)
	r.Run(":8000")
}