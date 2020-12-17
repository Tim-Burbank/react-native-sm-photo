
Pod::Spec.new do |s|
  s.name         = "PhotoPlugin"
  s.version      = "1.5.7"
  s.summary      = "PhotoPlugin"
  s.description  = <<-DESC
                   拍照插件
                   DESC
  s.homepage     = "https://github.com/Tim-Burbank/react-native-sm-photo"
  s.license      = "MIT"
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/PhotoPlugin.git", :tag => "1.5.7" }
  s.source_files  = "**/*.{h,m}"
  # s.resource_bundle = {
  #   'PhotoPlugin' => [
  #     'Resource/**/*.{png,strings,json}'
  #   ]
  # }
  s.resources    = 'Resource/PhotoPluginResources.bundle'
  s.requires_arc = true
  s.dependency "React"
end


