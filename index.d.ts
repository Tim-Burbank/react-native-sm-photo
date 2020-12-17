export declare type Action = 'selectAlbum' | 'recordVideo' | 'selectAvatar' | 'selectAlbumVideo' | 'takeVideo' | 'takePhoto' | 'selectAlbumPhotoAndVideo';
export declare type Option = {
    width: number;
    quality: number;
};
export default class RNPhotoPlugin {
    static Constants: {
        ACTION_SELECT_IMAGE: string;
        ACTION_TAKE_VIDEO: string;
        ACTION_SELECT_AVATAR: string;
        ACTION_SELECT_ALBUM_VIDEO: string;
        ACTION_SELECT_ALBUM_IMAGE_VIDEO: string;
        ACTION_TAKE_PHOTO: string;
        ACTION_CAPTURE_VIDEO: string;
        ACTION_CHANGE_LANGUAGE: string;
    };
    /**
     * android接口，请尽量不要直接使用此方法
     * @param action 操作
     * @param args 参数
     */
    static execute(action: Action, ...args: any[]): Promise<any>;
    /**
     * 选相册图片 （图片）
     * @param count 最大数量
     * @param width 宽
     * @param quality 质量, 范围1~100
     * @param minCount 最少数量
     */
    static selectImage(count: number, width: number, quality: number, minCount?: number): Promise<any>;
    /**
     * 验证的录视频 （视频）
     * @param text 显示的文本
     * @param quality 质量, 范围1~100
     */
    static recordVideo(text: string, quality?: number): Promise<any>;
    /**
     * 选头像 （图片）
     * @param width 宽
     * @param quantity 质量, 范围0 ~ 100
     */
    static selectAvatar(width: number, quantity: number): Promise<any>;
    /**
     * 选视频 （视频）
     * @param duration 时长，单位为秒
     * @param quantity 质量, 范围0 ~ 100
     * customCompress?: boolean, // 是否用自定义的压缩方式
     * vq_bitRate?: number // 比特率
     * vq_width?: number //视频宽度
     * vq_height?: number // 视频高度
     */
    static selectAlbumVideo({ maxDuration, videoQuality, customCompress, vq_bitRate, vq_width, vq_height, minDuration, }: {
        maxDuration: number;
        videoQuality?: number;
        customCompress?: boolean;
        vq_bitRate?: number;
        vq_width?: number;
        vq_height?: number;
        minDuration?: number;
    }): Promise<any>;
    /**
     * 选不压缩视频 （视频）
     * @param {number} duration
     * @returns {Promise<any>}
     */
    static selectUncompressedVideo(duration: number): Promise<any>;
    /**
     * 现场拍照 （图片）
     * @returns {Promise<string>} 照片路径
     * @param quantity 质量, 范围0 ~ 100
     * @param isFront, 打开摄像头是前置 or 后置
     */
    static takePhoto(width: number, quantity: number, isFront?: boolean): any;
    /**
     * 现场录像 (视频)
     * @param {number} minDuration 最小时长，单位为秒
     * @param {number} maxDuration 最大时长，单位为秒
     * @param quantity 质量, 范围0 ~ 100
     * @param isFront, 打开摄像头是前置 or 后置
     * @returns {any}
     */
    static takeVideo(minDuration: number, maxDuration: number, quantity?: number, isFront?: boolean): any;
    /**
     * 选视频&照片 (图片，视频，GIF)
     * @param {number} maxCount 最大数量
     * @param {number} width 宽
     * @param {number} picQuality 质量 0 - 100
     * @param {number} videoQuality 质量 0 - 100
     * @param {number} duration 时长，单位为秒
     * @param {number} gifSizeLimit 的大小限制,单位M
     * @param {number} minCount 最小数量
     * @returns {Promise<any>}
     */
    static selectAlbumPhotoAndVideo(maxCount: number, width: number, picQuality: number, videoQuality: number | undefined, maxDuration: number, gifSizeLimit: number, minCount?: number): any;
    /**
     * IOS only
     * maxCount,  // 选图片最大张数
     * width,         // 图片宽
     * picQuality,  // 图片压缩质量
     * videoQuality, //原来的压缩质量
     * maxDuration,  // 视频最大长度
     * gifSizeLimit,  // gif大小限制
     * customCompress?: boolean, // 是否用自定义的压缩方式
     * vid?: string, // 异步压缩的id  （传了这个参数说明是异步压缩，不传就是同步）
     * vq_bitRate?: number // 比特率
     * vq_width?: number //视频宽度
     * vq_height?: number // 视频高度
     * @returns {Promise<any>}
     */
    static IOS_selectAlbumPhotoAndVideoCanAsync({ minCount, maxCount, width, picQuality, videoQuality, maxDuration, gifSizeLimit, customCompress, vid, vq_bitRate, vq_width, vq_height }: {
        minCount?: number;
        maxCount: number;
        width: number;
        picQuality: number;
        videoQuality?: number;
        maxDuration: number;
        gifSizeLimit: number;
        customCompress?: boolean;
        vid?: string;
        vq_bitRate?: number;
        vq_width?: number;
        vq_height?: number;
    }): any;
    /**
     * 选视频&照片 (图片，视频，GIF) 视频未压缩
     * @param {number} maxCount 最大数量
     * @param {number} width 宽
     * @param {number} quality 质量 0 - 100
     * @param {number} duration 时长，单位为秒
     * @param {number} gifSizeLimit 的大小限制,单位M
     * @param {number} minCount 最小数量
     * @returns {Promise<any>}
     */
    static selectAlbumAndUncompressedVideo(maxCount: number, width: number, quality: number, maxDuration: number, gifSizeLimit: number, minCount?: number): any;
    /**
     * 上传表情（图片+GIF）
     * @param {number} count 数量
     * @param {number} width 宽
     * @param {number} quality 质量 0 - 100
     * @param {number} gifSizeLimit 的大小限制,单位M
     */
    static selectGifAndPic(count: number, width: number, quality: number, gifSizeLimit: number): any;
    /**
     * 压缩视频
     * @param path 视频路径
     * @param {number} quality 质量 0 - 100
     * @return (Promise<any>) 返回一个map对象 { 'original':(原始路径),'result':(压缩后路径)}
     */
    static compressVideo(path: string, maxDuration: number, quality?: number): any;
    /**
     * 设置插件语言
     * @param {'zh'|'en'} lang 语言, 'zh' 或 'en'
     */
    static setLang(lang: 'zh' | 'en'): any;
    static onProgress(cb: (...args: any[]) => void): void;
    static initStringConfig(config: any, defaultLanguage?: string): void;
    static initImageConfig(config: any, defaultLanguage?: string): void;
    static initColorConfig(config: any, defaultLanguage?: string): void;
    /**
     * iOS改变状态栏颜色：传入true，状态栏颜色变为白色，传入false，状态栏颜色变为黑色。
     */
    static setStatusBarLight(isLight: boolean): void;
}
